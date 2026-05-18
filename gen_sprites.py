"""Generate crystal-works sprites - pixel art for Crystal Works mod v1.1.0"""
from PIL import Image, ImageDraw
import os

COLOR_CRYSTAL = (101, 195, 232, 255)      # #65C3E8 - crystal blue
COLOR_CRYSTAL_DARK = (74, 142, 199, 255)  # #4A8EC7 - alloy blue
COLOR_WHITE = (255, 255, 255, 255)
COLOR_METAL = (140, 150, 160, 255)
COLOR_METAL_DARK = (90, 95, 100, 255)
COLOR_BG = (0, 0, 0, 0)  # transparent

BASE = "/home/zenxsin/cow/project/crystal-works/sprites"

def draw_crystal_item(draw, cx, cy, color):
    """Draw a crystal gem shape"""
    # Crystal shape: hexagon-like diamond
    pts = [
        (cx, cy - 12),      # top
        (cx + 8, cy - 4),   # top right
        (cx + 8, cy + 4),  # bottom right
        (cx, cy + 12),      # bottom
        (cx - 8, cy + 4),  # bottom left
        (cx - 8, cy - 4),  # top left
    ]
    draw.polygon(pts, fill=color, outline=COLOR_WHITE)
    # inner highlight
    inner = [
        (cx, cy - 6),
        (cx + 4, cy - 2),
        (cx + 4, cy + 2),
        (cx, cy + 6),
        (cx - 4, cy + 2),
        (cx - 4, cy - 2),
    ]
    hl = (min(color[0] + 40, 255), min(color[1] + 40, 255), min(color[2] + 40, 255), 255)
    draw.polygon(inner, fill=hl)

def draw_alloy_item(draw, cx, cy, color):
    """Draw a metallic ingot shape"""
    # Ingot body
    draw.rectangle([cx-8, cy-10, cx+8, cy+10], fill=color, outline=COLOR_WHITE)
    # Top highlight
    draw.rectangle([cx-6, cy-8, cx-8, cy-4], fill=(180, 190, 200, 255))
    # Actually proper ingot
    draw.polygon([
        (cx-10, cy-8), (cx+10, cy-8),
        (cx+6, cy+10), (cx-6, cy+10)
    ], fill=color, outline=COLOR_WHITE)
    hl = (min(color[0] + 60, 255), min(color[1] + 60, 255), min(color[2] + 60, 255), 255)
    draw.polygon([
        (cx-7, cy-6), (cx+7, cy-6),
        (cx+4, cy+2), (cx-4, cy+2)
    ], fill=hl)

def draw_crystal_drill(draw):
    """3x3 drill with crystal bits"""
    # Base frame (gray metal)
    draw.rectangle([8, 40, 88, 88], fill=COLOR_METAL_DARK, outline=COLOR_METAL)
    # Drill head (top, centered)
    draw.polygon([(48, 8), (40, 40), (56, 40)], fill=COLOR_METAL, outline=COLOR_METAL_DARK)
    # Crystal bits on drill head
    for x in [36, 48, 60]:
        draw.rectangle([x, 12, x+4, 20], fill=COLOR_CRYSTAL)
    # Crystal vein decoration on the body
    fornt
    for y in [48, 56, 64, 72]:
        draw.rectangle([40, y, 56, y+4], fill=COLOR_CRYSTAL)
    # Support legs
    draw.rectangle([12, 60, 20, 88], fill=COLOR_METAL)
    draw.rectangle([76, 60, 84, 88], fill=COLOR_METAL)

def draw_crystal_smelter(draw):
    """3x3 furnace with crystal flame"""
    # Furnace body
    draw.rectangle([12, 24, 84, 84], fill=COLOR_METAL_DARK, outline=COLOR_METAL)
    # Inner chamber
    draw.rectangle([24, 36, 72, 78], fill=(30, 30, 40, 255))
    # Crystal flame (central glow)
    draw.ellipse([36, 44, 60, 72], fill=COLOR_CRYSTAL)
    # Inner bright flame
    draw.ellipse([42, 50, 54, 66], fill=COLOR_WHITE)
    # Cross pattern on furnace
    draw.rectangle([24, 54, 72, 58], fill=COLOR_METAL)
    draw.rectangle([46, 36, 50, 78], fill=COLOR_METAL)
    # Pipes
    draw.rectangle([4, 40, 12, 56], fill=COLOR_METAL)
    draw.rectangle([84, 40, 92, 56], fill=COLOR_METAL)

def draw_crystal_mixer(draw):
    """3x3 liquid mixer"""
    # Base structure
    draw.rectangle([12, 50, 8, 76, 24], fill=COLOR_METAL_DARK)  # pipe
    # Mixer top (dome)
    draw.rectangle([16, 20, 80, 32], fill=COLOR_METAL, outline=COLOR_METAL_DARK)
    # Tank body
    draw.rectangle([20, 32, 76, 80], fill=COLOR_METAL_DARK, outline=COLOR_METAL)
    # Liquid inside
    draw.rectangle([24, 44, 72, 76], fill=(101, 195, 232, 180))
    # Bubbles
    for pos in [(30, 52, 4), (44, 48, 3), (58, 56, 5), (66,  (66, 50, 3)]:
        draw.ellipse([pos[0], pos[1], pos[0]+pos[2], pos[1]+pos[2]], fill=COLOR_WHITE)
    # Agitator shaft
    draw.rectangle([46, 32, 50, 44], fill=COLOR_METAL)
    # Paddle
    draw.rectangle([38, 40, 58, 44], fill=COLOR_METAL)

def draw_crystal_turret(draw):
    """3x3 turret"""
    # Base
    draw.rectangle([16, 60, 80, 88], fill=COLOR_METAL_DARK, outline=COLOR_METAL)
    # Turret body (rotating part)
    draw.ellipse([20, 36, 76, 72], fill=COLOR_METAL, outline=COLOR_METAL_DARK)
    # Crystal barrel
    draw.rectangle([40, 12, 56, 40], fill=COLOR_CRYSTAL_DARK, outline=COLOR_CRYSTAL)
    # Barrel tip glow
    draw.rectangle([42, 8, 54, 14], fill=COLOR_CRYSTAL)
    # Crystal core in body
    draw.ellipse([36, 46, 60, 66], fill=COLOR_CRYSTAL)
    draw.ellipse([42, 51, 54, 62], fill=COLOR_WHITE)
    # Side details
    draw.rectangle([16, 52, 22, 64], fill=COLOR_METAL)
    draw.rectangle([74, 52, 80, 64], fill=COLOR_METAL)

def draw_crystal_wall(draw, img_size):
    """1x1 wall (32x32)"""
    sz = img_size
    border = 2
    # Wall body
    draw.rectangle([border, border, sz-border-1, sz-border-1], fill=COLOR_CRYSTAL_DARK, outline=COLOR_CRYSTAL)
    # Crystal pattern- facets
    draw.polygon([
        (sz//2, border),  # top mid
        (sz-border-1, sz//2),  # right mid
        (sz//2, sz-border-1),  # bottom mid
        (border, sz//2)  # left mid
    ], fill=COLOR_CRYSTAL)
    # Center highlight
    draw.rectangle gt  #inner glow
    draw.polygon([
        (sz//2, sz//4),
        (3*sz//4, sz//2),
        (sz//2, 3*sz//4),
        (sz//4, sz//2)
    ], fill=(120,210,240,180))


def draw_rect_list: I see there are multiple syntax errors. Let me rewrite this properly.

BTW, I need to fix some issues in the script. Let me rewrite it cleanly.