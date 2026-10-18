"""Builds the Mekanism Fabricated project icon: the original Mekanism gear logo (rebuilt crisp at 512 px from the 96 px original)
with a red "FABRICATED" ribbon across the top-right corner. Usage: python make_icon.py <original icon.png> <output dir> [<original logo.png>]"""
import sys
from collections import Counter
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFilter, ImageFont

SIZE = 512
SUPER = 4  # supersampling factor for smooth edges
RIBBON_RED = (204, 32, 39)
RIBBON_EDGE = (150, 18, 24)
FONT = r"C:\Windows\Fonts\ariblk.ttf"


def rebuild_logo(original: Image.Image, size: int) -> Image.Image:
    """Upscale the flat-colour logo's mask, re-threshold it for crisp edges and fill it with the original colour."""
    original = original.convert("RGBA")
    big = size * SUPER
    alpha = original.getchannel("A").resize((big, big), Image.Resampling.BICUBIC)
    alpha = alpha.filter(ImageFilter.GaussianBlur(SUPER * 2.2)).point(lambda a: 255 if a >= 128 else 0)
    # Dominant opaque colour of the original
    color = Counter(p[:3] for p in original.get_flattened_data() if p[3] > 200).most_common(1)[0][0]
    layer = Image.new("RGBA", (big, big), color + (0,))
    layer.putalpha(alpha)
    return layer.resize((size, size), Image.Resampling.LANCZOS)


def ribbon(size: int) -> Image.Image:
    """A diagonal corner ribbon for the top-right corner, drawn supersampled."""
    big = size * SUPER
    band = int(big * 0.13)  # ribbon thickness
    offset = int(big * 0.40)  # distance of the ribbon's centre line from the corner, along each edge
    length = int(big * 1.2)
    strip = Image.new("RGBA", (length, band), (0, 0, 0, 0))
    draw = ImageDraw.Draw(strip)
    draw.rectangle([0, 0, length, band], fill=RIBBON_RED + (255,))
    edge = max(2, band // 14)
    draw.rectangle([0, 0, length, edge], fill=RIBBON_EDGE + (255,))
    draw.rectangle([0, band - edge, length, band], fill=RIBBON_EDGE + (255,))
    font = ImageFont.truetype(FONT, int(band * 0.46))
    text = "FABRICATED"
    box = draw.textbbox((0, 0), text, font=font)
    tw, th = box[2] - box[0], box[3] - box[1]
    draw.text(((length - tw) / 2 - box[0], (band - th) / 2 - box[1]), text, font=font, fill=(255, 255, 255, 255))
    rotated = strip.rotate(-45, resample=Image.Resampling.BICUBIC, expand=True)
    # Centre of the ribbon's centre line: midpoint between (big - offset, 0) and (big, offset); compose on a larger canvas, then crop
    cx, cy = big - offset / 2, offset / 2
    tmp = Image.new("RGBA", (big * 3, big * 3), (0, 0, 0, 0))
    tmp.alpha_composite(rotated, (int(big + cx - rotated.width / 2), int(big + cy - rotated.height / 2)))
    layer = tmp.crop((big, big, big * 2, big * 2))
    # Soft shadow under the ribbon
    shadow = layer.getchannel("A").filter(ImageFilter.GaussianBlur(SUPER * 3))
    shadow_layer = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    shadow_layer.putalpha(shadow.point(lambda a: a * 45 // 100))
    shadow_layer = ImageChops.offset(shadow_layer, 0, SUPER * 3)
    out = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    out.alpha_composite(shadow_layer)
    out.alpha_composite(layer)
    return out.resize((size, size), Image.Resampling.LANCZOS)


def main():
    original = Image.open(sys.argv[1])
    out_dir = Path(sys.argv[2])
    out_dir.mkdir(parents=True, exist_ok=True)
    margin = int(SIZE * 0.06)
    logo = rebuild_logo(original, SIZE - 2 * margin)
    ribbon_layer = ribbon(SIZE)
    for name, background in [("icon.png", (0, 0, 0, 0)), ("icon_dark.png", (24, 26, 30, 255))]:
        icon = Image.new("RGBA", (SIZE, SIZE), background)
        icon.alpha_composite(logo, (margin, margin))
        icon.alpha_composite(ribbon_layer)
        icon.save(out_dir / name)
        print("wrote", out_dir / name)
    if len(sys.argv) > 3:
        # Wordmark variant: the original wide MEKANISM logo (white on transparent) on a dark square
        wordmark = Image.open(sys.argv[3]).convert("RGBA")
        width = int(SIZE * 0.92)
        height = round(wordmark.height * width / wordmark.width)
        wordmark = wordmark.resize((width, height), Image.Resampling.LANCZOS)
        icon = Image.new("RGBA", (SIZE, SIZE), (24, 26, 30, 255))
        icon.alpha_composite(wordmark, ((SIZE - width) // 2, int(SIZE * 0.58 - height / 2)))
        icon.alpha_composite(ribbon_layer)
        icon.save(out_dir / "icon_wordmark.png")
        print("wrote", out_dir / "icon_wordmark.png")


if __name__ == "__main__":
    main()
