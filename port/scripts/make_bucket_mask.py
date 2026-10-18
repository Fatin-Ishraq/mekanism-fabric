#!/usr/bin/env python3
"""Generates the bucket fluid overlay texture used by the port's neoforge:fluid_container model loader.

NeoForge ships its own mask textures; we derive an equivalent from vanilla: every pixel where water_bucket.png differs from bucket.png
is fluid. The output keeps the water pixels' brightness (greyscale) so the overlay can be tinted with the fluid colour (tint index 1).
Usage: make_bucket_mask.py <minecraft-client.jar> <output.png>
"""
import struct
import sys
import zipfile
import zlib


def read_png(data):
    assert data[:8] == b'\x89PNG\r\n\x1a\n'
    pos, idat, width, height, color_type, palette, trns, depth = 8, b'', 0, 0, 0, None, None, 8
    while pos < len(data):
        length = struct.unpack('>I', data[pos:pos + 4])[0]
        ctype = data[pos + 4:pos + 8]
        chunk = data[pos + 8:pos + 8 + length]
        if ctype == b'IHDR':
            width, height, depth, color_type = struct.unpack('>IIBB', chunk[:10])
            assert depth == 8 or color_type == 3, 'only 8-bit or palette PNGs supported'
        elif ctype == b'PLTE':
            palette = [tuple(chunk[i:i + 3]) for i in range(0, len(chunk), 3)]
        elif ctype == b'tRNS':
            trns = chunk
        elif ctype == b'IDAT':
            idat += chunk
        pos += 12 + length
    channels = {6: 4, 2: 3, 3: 1, 0: 1, 4: 2}[color_type]
    raw = zlib.decompress(idat)
    bits = depth * channels
    stride = (width * bits + 7) // 8
    bpp = max(1, bits // 8)
    rows, prev = [], bytearray(stride)
    i = 0
    for _ in range(height):
        f = raw[i]
        line = bytearray(raw[i + 1:i + 1 + stride])
        i += 1 + stride
        for x in range(stride):
            a = line[x - bpp] if x >= bpp else 0
            b = prev[x]
            c = prev[x - bpp] if x >= bpp else 0
            if f == 1:
                line[x] = (line[x] + a) & 0xFF
            elif f == 2:
                line[x] = (line[x] + b) & 0xFF
            elif f == 3:
                line[x] = (line[x] + (a + b) // 2) & 0xFF
            elif f == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                pred = a if pa <= pb and pa <= pc else (b if pb <= pc else c)
                line[x] = (line[x] + pred) & 0xFF
        rows.append(line)
        prev = line
    pixels = []
    for line in rows:
        row = []
        for x in range(width):
            if depth < 8:
                per_byte = 8 // depth
                byte = line[x // per_byte]
                shift = 8 - depth * (x % per_byte + 1)
                px = [(byte >> shift) & ((1 << depth) - 1)]
            else:
                px = line[x * channels:(x + 1) * channels]
            if color_type == 6:
                row.append(tuple(px))
            elif color_type == 2:
                row.append((px[0], px[1], px[2], 255))
            elif color_type == 3:
                r, g, b = palette[px[0]]
                a = trns[px[0]] if trns and px[0] < len(trns) else 255
                row.append((r, g, b, a))
            elif color_type == 0:
                row.append((px[0], px[0], px[0], 255))
            else:
                row.append((px[0], px[0], px[0], px[1]))
        pixels.append(row)
    return width, height, pixels


def write_png(path, width, height, pixels):
    raw = b''.join(b'\x00' + bytes(v for p in row for v in p) for row in pixels)

    def chunk(t, d):
        return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xFFFFFFFF)

    png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)) + chunk(b'IDAT', zlib.compress(raw)) + chunk(b'IEND', b'')
    open(path, 'wb').write(png)


def main():
    jar = zipfile.ZipFile(sys.argv[1])
    w, h, empty = read_png(jar.read('assets/minecraft/textures/item/bucket.png'))
    _, _, water = read_png(jar.read('assets/minecraft/textures/item/water_bucket.png'))
    out = []
    for y in range(h):
        row = []
        for x in range(w):
            if water[y][x] != empty[y][x] and water[y][x][3] > 0:
                r, g, b, _ = water[y][x]
                # Normalise water's blue-ish shading into a greyscale brightness map
                lum = max(r, g, b)
                v = min(255, int(lum * 255 / 230))
                row.append((v, v, v, 255))
            else:
                row.append((0, 0, 0, 0))
        out.append(row)
    write_png(sys.argv[2], w, h, out)
    print(f"wrote {sys.argv[2]} ({sum(1 for r in out for p in r if p[3])} fluid pixels)")


if __name__ == '__main__':
    main()
