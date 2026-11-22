"""Tiny software renderer for Minecraft JSON block models (elements, face UVs, single-axis element rotations),
for look-approval renders without launching the game. Orthographic camera (yaw / pitch), z-buffer, Minecraft-style
face shading. Namespaces: creativeengineering -> this repo's assets; create -> ~/ref/create (preview only, never
committed); minecraft -> ~/ref/excalibur.

Usage from Python:
    sc = Scene(yaw=-35, pitch=30, scale=4)
    sc.add('creativeengineering:block/diesel_crankcase', (0, 0, 0))
    sc.render('out.png')
"""
import json
import math
import os

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
ROOTS = {
    'creativeengineering': os.path.join(REPO, 'mod/src/main/resources/assets/creativeengineering'),
    'create': os.path.expanduser('~/ref/create/src/main/resources/assets/create'),
    'minecraft': os.path.expanduser('~/ref/excalibur/assets/minecraft'),
}
_tex_cache = {}


def _path(rl, kind, ext):
    ns, p = rl.split(':', 1) if ':' in rl else ('minecraft', rl)
    return os.path.join(ROOTS[ns], kind, p + ext)


def texture(rl):
    if rl not in _tex_cache:
        im = Image.open(_path(rl, 'textures', '.png')).convert('RGBA')
        w = im.width
        _tex_cache[rl] = np.asarray(im.crop((0, 0, w, w)), dtype=np.float32)   # first frame of animated strips
    return _tex_cache[rl]


def load_model(rl):
    with open(_path(rl, 'models', '.json')) as f:
        return json.load(f)


def cube(tex_side, tex_top=None, tex_bottom=None, frm=(0, 0, 0), to=(16, 16, 16)):
    """An element list for a simple textured box (used for preview-only neighbours like a Create tank)."""
    tt, tb = tex_top or tex_side, tex_bottom or tex_top or tex_side
    return {'textures': {'s': tex_side, 't': tt, 'b': tb},
            'elements': [{'from': list(frm), 'to': list(to), 'faces': {
                d: {'texture': '#t' if d == 'up' else '#b' if d == 'down' else '#s'}
                for d in ('north', 'south', 'east', 'west', 'up', 'down')}}]}


# face -> (uv origin corner, u direction, v direction, outward normal), in element space, from (x1..x2, y1..y2, z1..z2)
def _face_frame(d, a, b):
    x1, y1, z1 = a
    x2, y2, z2 = b
    return {
        'north': ((x2, y2, z1), (-1, 0, 0), (0, -1, 0), (0, 0, -1), (16 - x2, 16 - y2, 16 - x1, 16 - y1)),
        'south': ((x1, y2, z2), (1, 0, 0), (0, -1, 0), (0, 0, 1), (x1, 16 - y2, x2, 16 - y1)),
        'west': ((x1, y2, z1), (0, 0, 1), (0, -1, 0), (-1, 0, 0), (z1, 16 - y2, z2, 16 - y1)),
        'east': ((x2, y2, z2), (0, 0, -1), (0, -1, 0), (1, 0, 0), (16 - z2, 16 - y2, 16 - z1, 16 - y1)),
        'up': ((x1, y2, z1), (1, 0, 0), (0, 0, 1), (0, 1, 0), (x1, z1, x2, z2)),
        'down': ((x1, y1, z2), (1, 0, 0), (0, 0, -1), (0, -1, 0), (x1, 16 - z2, x2, 16 - z1)),
    }[d]


def _rot(p, rot):
    if not rot:
        return np.array(p, dtype=float)
    o = np.array(rot['origin'], dtype=float)
    q = np.array(p, dtype=float) - o
    t = math.radians(rot['angle'])
    c, s = math.cos(t), math.sin(t)
    ax = rot['axis']
    x, y, z = q
    if ax == 'x':
        q = np.array([x, y * c - z * s, y * s + z * c])
    elif ax == 'y':
        q = np.array([x * c + z * s, y, -x * s + z * c])
    else:
        q = np.array([x * c - y * s, x * s + y * c, z])
    return q + o


class Scene:
    def __init__(self, yaw=-35.0, pitch=30.0, scale=4.0):
        self.yaw, self.pitch, self.scale = math.radians(yaw), math.radians(pitch), scale
        self.quads = []          # (corners3 world px-units, uvrect, texture array, shade)
        self.puffs = []

    def add(self, model, pos, yrot=0, zrot=0, xform=None):
        """Place a model (resource location or dict) at block pos. zrot then yrot (degrees) turn it about the block centre,
        like a blockstate's x / y rotations (zrot 90 lays an upward-facing model on its side, pointing -X)."""
        m = load_model(model) if isinstance(model, str) else model
        tex = dict(m.get('textures', {}))

        def resolve(ref):
            seen = 0
            while ref.startswith('#') and seen < 8:
                ref = tex.get(ref[1:], ref)
                seen += 1
            return ref

        for el in m.get('elements', []):
            a, b, rot = el['from'], el['to'], el.get('rotation')
            for d, f in el['faces'].items():
                p0, du, dv, n, uv_default = _face_frame(d, a, b)
                uv = f.get('uv', uv_default)
                lu = abs((b[0] - a[0]) * du[0]) + abs((b[1] - a[1]) * du[1]) + abs((b[2] - a[2]) * du[2])
                lv = abs((b[0] - a[0]) * dv[0]) + abs((b[1] - a[1]) * dv[1]) + abs((b[2] - a[2]) * dv[2])
                pts = [np.array(p0, float), np.array(p0, float) + np.array(du) * lu, np.array(p0, float) + np.array(dv) * lv]
                pts = [_rot(p, rot) for p in pts]
                nrm = _rot(np.array(n, float) + (rot['origin'] if rot else 0), rot) - (np.array(rot['origin']) if rot else 0)
                if xform:
                    pts = [xform(p) for p in pts]
                    nrm = xform(nrm + 8) - xform(np.array([8.0, 8, 8]))
                if zrot:
                    zr = {'origin': [8, 8, 8], 'axis': 'z', 'angle': zrot}
                    pts = [_rot(p, zr) for p in pts]
                    nrm = _rot(nrm + 8, zr) - 8
                if yrot:
                    t = math.radians(-yrot)
                    c, s = math.cos(t), math.sin(t)
                    pts = [np.array([(p[0] - 8) * c - (p[2] - 8) * s + 8, p[1], (p[0] - 8) * s + (p[2] - 8) * c + 8]) for p in pts]
                    nrm = np.array([nrm[0] * c - nrm[2] * s, nrm[1], nrm[0] * s + nrm[2] * c])
                off = np.array(pos, float) * 16
                pts = [p + off for p in pts]
                ref = resolve(f['texture'])
                if ref.startswith('#'):
                    continue
                self.quads.append((pts, uv, texture(ref), nrm, f.get('rotation', 0)))

    def puff(self, pos, r, alpha):
        """A smoke puff (block coords, radius in blocks) drawn over the render."""
        self.puffs.append((np.array(pos, float) * 16, r * 16, alpha))

    def _cam(self, p):
        c, s = math.cos(self.yaw), math.sin(self.yaw)
        x1, z1 = p[0] * c - p[2] * s, p[0] * s + p[2] * c
        y1 = p[1]
        cp, sp = math.cos(self.pitch), math.sin(self.pitch)
        return np.array([x1, y1 * cp - z1 * sp, y1 * sp + z1 * cp])   # screen x, screen y (up), depth (toward camera)

    def render(self, out, size=(1100, 760), bg=('#3a4048', '#22262b'), margin=40, ground=None):
        cams = [[self._cam(p) for p in q[0]] for q in self.quads]
        allp = np.array([c for cs in cams for c in cs] + [self._cam(p[0]) for p in self.puffs] or [[0, 0, 0]])
        minx, maxx = allp[:, 0].min(), allp[:, 0].max()
        miny, maxy = allp[:, 1].min(), allp[:, 1].max()
        k = min((size[0] - 2 * margin) / max(1, maxx - minx), (size[1] - 2 * margin) / max(1, maxy - miny), self.scale)
        W, H = size
        ox = W / 2 - (minx + maxx) / 2 * k
        oy = H / 2 + (miny + maxy) / 2 * k
        img = np.zeros((H, W, 3), np.float32)
        top, bot = [np.array([int(h[i:i + 2], 16) for i in (1, 3, 5)], np.float32) for h in bg]
        for y in range(H):
            img[y] = top + (bot - top) * (y / H)
        zbuf = np.full((H, W), -1e9, np.float32)
        for (pts, uv, tex, nrm, uvrot), cs in zip(self.quads, cams):
            n = nrm / (np.linalg.norm(nrm) or 1)
            nc = self._cam(n) - self._cam(np.zeros(3))
            if nc[2] <= 1e-6:
                continue                                           # back face
            shade = 0.6 * n[0] ** 2 + 0.8 * n[2] ** 2 + (1.0 if n[1] > 0 else 0.5) * n[1] ** 2
            a = np.array([ox + cs[0][0] * k, oy - cs[0][1] * k])
            bu = np.array([cs[1][0] * k, -cs[1][1] * k]) - np.array([cs[0][0] * k, -cs[0][1] * k])
            bv = np.array([cs[2][0] * k, -cs[2][1] * k]) - np.array([cs[0][0] * k, -cs[0][1] * k])
            det = bu[0] * bv[1] - bu[1] * bv[0]
            if abs(det) < 1e-6:
                continue
            corners = [a, a + bu, a + bv, a + bu + bv]
            x0 = max(0, int(min(c[0] for c in corners)) - 1); x1 = min(W, int(max(c[0] for c in corners)) + 2)
            y0 = max(0, int(min(c[1] for c in corners)) - 1); y1 = min(H, int(max(c[1] for c in corners)) + 2)
            if x0 >= x1 or y0 >= y1:
                continue
            gx, gy = np.meshgrid(np.arange(x0, x1) + 0.5, np.arange(y0, y1) + 0.5)
            px, py = gx - a[0], gy - a[1]
            s = (px * bv[1] - py * bv[0]) / det
            t = (bu[0] * py - bu[1] * px) / det
            inside = (s >= 0) & (s <= 1) & (t >= 0) & (t <= 1)
            if not inside.any():
                continue
            depth = cs[0][2] + s * (cs[1][2] - cs[0][2]) + t * (cs[2][2] - cs[0][2])
            th = tex.shape[0]
            ss, tt = {0: (s, t), 90: (t, 1 - s), 180: (1 - s, 1 - t), 270: (1 - t, s)}[uvrot % 360]
            u = uv[0] + ss * (uv[2] - uv[0])
            v = uv[1] + tt * (uv[3] - uv[1])
            ti = np.clip((u * th / 16).astype(int), 0, th - 1)
            tj = np.clip((v * th / 16).astype(int), 0, th - 1)
            col = tex[tj, ti]
            ok = inside & (col[..., 3] > 10) & (depth > zbuf[y0:y1, x0:x1])
            zb = zbuf[y0:y1, x0:x1]
            zb[ok] = depth[ok]
            reg = img[y0:y1, x0:x1]
            reg[ok] = col[ok][:, :3] * shade
        out_img = Image.fromarray(np.clip(img, 0, 255).astype(np.uint8))
        if self.puffs:
            layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
            dr = ImageDraw.Draw(layer)
            for p, r, al in self.puffs:
                c = self._cam(p)
                cx, cy, rr = ox + c[0] * k, oy - c[1] * k, r * k
                dr.ellipse((cx - rr, cy - rr, cx + rr, cy + rr), fill=(70, 70, 72, int(al * 255)))
            layer = layer.filter(ImageFilter.GaussianBlur(radius=float(max(1.0, k * 0.8))))
            out_img = Image.alpha_composite(out_img.convert('RGBA'), layer)
        out_img.save(out)
        return out
