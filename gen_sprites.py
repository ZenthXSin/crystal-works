"""Generate crystal-works pixel art sprites"""
from PIL import Image, ImageDraw
import os

BLUE = (101, 195, 232, 255)
BLUE_D = (74, 142, 199, 255)
WHITE = (255, 255, 255, 255)
METAL = (140, 150, 160, 255)
METAL_D = (90, 95, 100, 255)
BASE = "/home/zenxsin/cow/project/crystal-works/sprites"


def item_crystal():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    cx, cy = 16, 16
    # Hexagon crystal
    pts = [(cx, cy - 12), (cx + 8, cy - 4), (cx + 8, cy + 4),
           (cx, cy + 12), (cx - 8, cy + 4), (cx - 8, cy - 4)]
    d.polygon(pts, fill=BLUE, outline=WHITE)
    # Inner highlight
    hi = [(cx, cy - 6), (cx + 4, cy - 2), (cx + 4, cy + 2),
          (cx, cy + 6), (cx - 4, cy + 2), (cx - 4, cy - 2)]
    d.polygon(hi, fill=(141, 215, 255, 255))
    img.save(BASE + "/items/crystal.png")


def item_alloy():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    cx, cy = 16, 16
    # Ingot shape
    d.polygon([(cx - 10, cy - 8), (cx + 10, cy - 8),
               (cx + 6, cy + 10), (cx - 6, cy + 10)],
              fill=BLUE_D, outline=WHITE)
    d.polygon([(cx - 7, cy - 6), (cx + 7, cy - 6),
               (cx + 4, cy + 2), (cx - 4, cy + 2)],
              fill=(134, 182, 219, 255))
    img.save(BASE + "/items/crystal-alloy.png")


def block_drill():
    img = Image.new("RGBA", (96, 96), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Base frame
    d.rectangle([8, 40, 88, 88], fill=METAL_D, outline=METAL)
    # Drill head
    d.polygon([(48, 8), (36, 40), (60, 40)], fill=METAL, outline=METAL_D)
    d.polygon([(48, 12), (40, 36), (56, 36)], fill=BLUE)
    # Crystal bits on head
    for x in [36, 48, 60]:
        d.rectangle([x, 12, x + 4, 20], fill=BLUE)
    # Crystal veins
    for y in range(48, 80, 8):
        d.rectangle([36, y, 60, y + 4], fill=BLUE)
    # Legs
    d.rectangle([12, 60, 20, 88], fill=METAL)
    d.rectangle([76, 60, 84, 88], fill=METAL)
    img.save(BASE + "/blocks/production/crystal-drill.png")


def block_smelter():
    img = Image.new("RGBA", (96, 96), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Body
    d.rectangle([12, 24, 84, 84], fill=METAL_D, outline=METAL)
    d.rectangle([24, 36, 72, 78], fill=(30, 30, 40, 255))
    # Flame
    d.ellipse([36, 44, 60, 72], fill=BLUE)
    d.ellipse([42, 50, 54, 66], fill=(180, 230, 255, 255))
    # Cross frame
    d.rectangle([24, 54, 72, 58], fill=METAL)
    d.rectangle([46, 36, 50, 78], fill=METAL)
    # Pipes
    d.rectangle([4, 40, 12, 60], fill=METAL)
    d.rectangle([84, 40, 92, 60], fill=METAL)
    img.save(BASE + "/blocks/production/crystal-smelter.png")


def block_mixer():
    img = Image.new("RGBA", (96, 96), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Top dome
    d.rectangle([16, 20, 80, 32], fill=METAL)
    # Tank
    d.rectangle([20, 32, 76, 80], fill=METAL_D, outline=METAL)
    # Liquid

    d.rectangle([24, 44, 72, 76], fill=(101, 195, 232, 160))
    # Bubbles
    for x, y, r in [(30, 52, 4), (44, 48, 3), (58, 56, 5), (60, 50, 3)]:
        d.ellipse([x, y, x + r, y + r], fill=WHITE)
    # Agitator
    d.rectangle([46, 32, 50, 44], fill=METAL)
    d.rectangle([36, 38, 60, 42], fill=METAL)
    img.save(BASE + "/blocks/production/crystal-fluid-mixer.png")


def block_turret():
    img = Image.new("RGBA", (96, 96), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Base
    d.rectangle([16, 60, 80, 88], fill=METAL_D, outline=METAL)
    # Rotating body
    d.ellipse([20, 36, 76, 72], fill=METAL, outline=METAL_D)
    # Barrel
    d.rectangle([40, 12, 56, 38], fill=BLUE_D)
    d.rectangle([42, 8, 54, 14], fill=BLUE)  # tip glow
    # Crystal core
    d.ellipse([36, 46, 60, 66], fill=BLUE)
    d.ellipse([42, 51, 54, 62], fill=(180, 230, 255, 255))
    # Side mounts
    d.rectangle([14, 52, 24, 68], fill=METAL)
    d.rectangle([72, 52, 82, 68], fill=METAL)
    img.save(BASE + "/blocks/turrets/crystal-turret.png")


def block_wall():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Body
    d.rectangle([2, 2, 29, 29], fill=BLUE_D, outline=BLUE)
    # Diamond facet
    d.polygon([(16, 2), (29, 16), (16, 29), (2, 16)],
              fill=(81, 155, 215, 255))
    d.polygon([(16, 6), (26, 16), (16, 26), (6, 16)],
              fill=(91, 165, 225, 255))
    d.polygon([(16, 8), (24, 16), (16, 24), (8, 16)],
              fill=(141, 215, 255, 180))
    img.save(BASE + "/blocks/defense/crystal-wall.png")


def block_wall_large():
    """2x2 wall (64x64)"""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 2, 61, 61], fill=BLUE_D, outline=BLUE, width=2)
    # 4 quadrants each with a diamond facet
    for ox, oy in [(0,0), (32,0), (0,32), (32,32)]:
        cx, cy = ox + 32, oy + 32
        d.polygon([(ox+16, oy+2), (ox+29, oy+16), (ox+16, oy+29), (ox+2, oy+16)],
                  fill=(81, 155, 215, 255))
        d.polygon([(ox+16, oy+6), (ox+26, oy+16), (ox+16, oy+26), (ox+6, oy+16)],
                  fill=(91, 165, 225, 255))
    # Cross joint
    d.rectangle([30, 2, 33, 61], fill=BLUE)
    d.rectangle([2, 30, 61, 33], fill=BLUE)
    img.save(BASE + "/blocks/defense/crystal-wall-large.png")


def block_borer():
    """4x4 burst drill (128x128)"""
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([16, 80, 112, 120], fill=METAL_D, outline=METAL)
    d.rectangle([24, 44, 104, 84], fill=METAL_D, outline=METAL)
    # Three drill heads
    for dx, dy in [(24, 20), (56, 8), (88, 20)]:
        d.rectangle([dx + 4, dy + 16, dx + 12, dy + 40], fill=METAL)
        d.polygon([(dx + 2, dy + 16), (dx + 14, dy + 16), (dx + 8, dy)], fill=BLUE)
        d.rectangle([dx + 2, dy + 4, dx + 14, dy + 10], fill=BLUE_D)
    # Crystal veins
    for y in range(52, 80, 6):
        d.rectangle([32, y, 96, y + 3], fill=BLUE)
    d.ellipse([52, 52, 76, 76], fill=(101, 195, 232, 100))
    d.ellipse([58, 58, 70, 70], fill=(180, 230, 255, 150))
    for x in [20, 100]:
        d.rectangle([x, 104, x + 12, 120], fill=METAL)
        d.rectangle([x - 4, 116, x + 16, 120], fill=METAL_D)
    img.save(BASE + "/blocks/production/crystal-borer.png")


if __name__ == "__main__":
    item_crystal()
    item_alloy()
    block_drill()
    block_smelter()
    block_mixer()
    block_turret()
    block_wall()
    block_wall_large()
    block_borer()
    print("All sprites regenerated!")