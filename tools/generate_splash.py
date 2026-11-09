#!/usr/bin/env python3
"""Render Figma 16:4's gradient layers, without external dependencies.

Source: https://www.figma.com/design/FZ3mtesFGEHyt1yL3une6K?node-id=16-3
The surrounding browser canvas, border and shadow are not device content.
Run from any directory to regenerate both platforms' identical 2x artwork.
"""
from math import erf, exp, sqrt
from pathlib import Path
import struct
import zlib

ROOT = Path(__file__).resolve().parents[1]
WIDTH, HEIGHT = 824, 1768


def mix(background, foreground, alpha):
    return tuple(a * (1 - alpha) + b * alpha for a, b in zip(background, foreground))


def blurred_interval(value, start, end, sigma):
    return (erf((value - start) / (sigma * sqrt(2))) -
            erf((value - end) / (sigma * sqrt(2)))) / 2


def chunk(kind, data):
    return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data))


def render():
    pixels = bytearray()
    cyan_x = [blurred_interval((x + .5) / 2, -112, 272, 64) for x in range(WIDTH)]
    blue_x = [blurred_interval((x + .5) / 2, 188, 508, 64) for x in range(WIDTH)]
    bottom_x = [blurred_interval((x + .5) / 2, 0, 412, 24) for x in range(WIDTH)]
    kernel = [(d, exp(-.5 * (d / 24) ** 2)) for d in range(-96, 97, 2)]
    kernel_total = sum(weight for _, weight in kernel)
    for y in range(HEIGHT):
        py = (y + .5) / 2
        t = py / 884
        base = mix((255, 255, 255), (240, 247, 253), t * 2) if t < .5 else mix((240, 247, 253), (228, 241, 252), (t - .5) * 2)
        cyan_y = blurred_interval(py, -112, 272, 64)
        blue_y = blurred_interval(py, 295, 615, 64)
        # Bottom gradient extends 64pt beyond the screen, as in the source.
        premultiplied = [0., 0., 0.]
        bottom_alpha = 0.
        for delta, weight in kernel:
            sample_y = py + delta
            if not 692 <= sample_y <= 948:
                continue
            bottom_t = (sample_y - 692) / 256
            alpha = bottom_t * .5 if bottom_t < .5 else .25 + (bottom_t - .5) * 1.4
            color = (156, 240, 255) if bottom_t < .5 else mix((156, 240, 255), (255, 255, 255), (bottom_t - .5) * 2)
            bottom_alpha += alpha * weight / kernel_total
            for channel in range(3):
                premultiplied[channel] += color[channel] * alpha * weight / kernel_total
        bottom_color = tuple(c / bottom_alpha for c in premultiplied) if bottom_alpha else (0, 0, 0)
        pixels.append(0)  # PNG scanline filter: none
        for x in range(WIDTH):
            color = base
            # The source uses two single 3.125% gradient strips (not a tiled grid).
            if py < 884 / 32:
                color = mix(color, (0, 104, 117), .07 * .85)
            if (x + .5) / 2 < 412 / 32:
                color = mix(color, (0, 104, 117), .07 * .85)
            color = mix(color, (0, 227, 253), .2 * cyan_x[x] * cyan_y)
            color = mix(color, (56, 189, 248), .2 * blue_x[x] * blue_y)
            color = mix(color, bottom_color, bottom_alpha * bottom_x[x])
            pixels.extend(round(c) for c in color)
    return (b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', WIDTH, HEIGHT, 8, 2, 0, 0, 0)) +
            chunk(b'IDAT', zlib.compress(pixels, 9)) + chunk(b'IEND', b''))


if __name__ == '__main__':
    artwork = render()
    for relative in ('app/src/main/res/drawable-nodpi/splash_background.png',
                     'iosApp/Assets.xcassets/SplashBackground.imageset/splash_background.png'):
        path = ROOT / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(artwork)
        print(path)
