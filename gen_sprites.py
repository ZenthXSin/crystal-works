#!/usr/bin/env python3
from PIL import Image, ImageDraw
import math, os

B = os.path.dirname(os.path.abspath(__file__)) + '/sprites'
PC = (0x65, 0xC3, 0xE8)
PD = (0x4A, 0x90, 0xD9)
PL = (0xB8, 0xF0, 0xFF)

def r(c, a=255):
    return (c[0], c[1], c[2], a)

def hexp(cx, cy, radius):
    return [(cx + radius * math.cos(math.radians(60*i - 90)),
             cy + radius * math.sin(math.radians(60*i - 90))) for i in range(6)]

def sv(sub, name, img):
    path = os.path.join(B, sub, name)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)

# items
for nm, c1 in [('crystal', PC), ('crystal-alloy', (0x5D, 0x8A, 0xA8))]:
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.polygon(hexp(16, 16, 13), fill=r(c1), outline=r(PL, 200))
    d.polygon(hexp(16, 16, 7), fill=r(PD if nm == 'crystal' else (0x3A, 0x6A, 0x88)))
    d.line([(10, 16), (22, 16)], fill=r(PL, 150), width=1)
    d.line([(16, 10), (16, 22)], fill=r(PL, 150), width=1)
    sv('items', nm + '.png', im)

# ore
im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rectangle([0, 0, 31, 31], fill=r((64, 64, 64)), outline=r((32, 48, 32)))
for x, y, s in [(6, 8, 1), (16, 5, 1.2), (24, 9, 0.8), (12, 18, 0.9), (22, 20, 1.1)]:
    cc = (int(PC[0]*0.7), int(PC[1]*0.7), int(PC[2]*0.7))
    d.polygon([(x, y - int(6*s)), (x + int(4*s), y), (x - int(4*s), y)], fill=r(cc))
    d.line([(x, y - int(6*s)), (x + int(2*s), y - 1)], fill=r(PL, 100), width=1)
sv('blocks/environment', 'crystal-ore.png', im)

# wall
im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rectangle([1, 1, 30, 30], fill=r(PD), outline=r(PC))
for pts, c in [
    ([(4, 1), (8, 10), (2, 10)], PC),
    ([(10, 1), (14, 12), (6, 12)], PL),
    ([(16, 1), (22, 10), (12, 10)], PC),
    ([(22, 1), (28, 12), (18, 12)], (0x2C, 0x5C, 0x9A)),
    ([(27, 1), (31, 9), (23, 9)], PC),
]:
    d.polygon(pts, fill=r(c))
d.line([(8, 12), (8, 28)], fill=r(PL, 80), width=1)
d.line([(20, 10), (20, 26)], fill=r(PL, 60), width=1)
sv('blocks/defense', 'crystal-wall.png', im)

# wall large
im = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rectangle([1, 1, 62, 62], fill=r(PD), outline=r(PC, 200))
d.line([(31, 1), (31, 62)], fill=r((0x2C, 0x5C, 0x9A), 150), width=1)
d.line([(1, 31), (62, 31)], fill=r((0x2C, 0x5C, 0x9A), 150), width=1)
for ox in [0, 32]:
    d.polygon([(ox+4, 1), (ox+10, 12), (ox+1, 12)], fill=r(PC))
    d.polygon([(ox+14, 1), (ox+22, 14), (ox+8, 14)], fill=r(PL, 200))
    d.polygon([(ox+24, 1), (ox+31, 12), (ox+20, 12)], fill=r(PC))
for ox, oy in [(8, 8), (40, 8), (8, 40), (40, 40)]:
    d.polygon([(ox, oy), (ox+16, oy+8), (ox+8, oy+20), (ox-8, oy+8)], fill=r(PC, 80))
sv('blocks/defense', 'crystal-wall-large.png', im)

# turret
im = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.ellipse([8, 20, 56, 56], fill=r(PD), outline=r(PC, 200))
d.ellipse([16, 28, 48, 48], fill=r((0x3A, 0x6A, 0x88)), outline=r(PC, 100))
d.rectangle([22, 6, 42, 28], fill=r((80, 96, 112)), outline=r(PC, 150))
d.rectangle([22, 6, 42, 12], fill=r(PC))
d.polygon([(26, 6), (32, 2), (38, 6), (32, 10)], fill=r(PL))
d.ellipse([28, 4, 36, 12], fill=r(PD))
sv('blocks/turrets', 'crystal-turret.png', im)

# smelter
im = Image.new('RGBA', (96, 96), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rounded_rectangle([4, 10, 92, 92], radius=6, fill=r((48, 64, 80)), outline=r(PC, 180))
d.ellipse([20, 20, 76, 76], outline=r(PC, 200), width=2)
d.ellipse([24, 24, 72, 72], fill=r(PC, 80))
d.ellipse([34, 34, 62, 62], fill=r(PL, 40))
d.rectangle([10, 44, 20, 52], fill=r(PL, 100))
d.rectangle([76, 44, 86, 52], fill=r(PL, 100))
d.rectangle([44, 10, 52, 20], fill=r(PL, 100))
sv('blocks/production', 'crystal-smelter.png', im)

# drill
im = Image.new('RGBA', (96, 96), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rounded_rectangle([4, 60, 92, 92], radius=4, fill=r((80, 96, 112)), outline=r(PC, 150))
d.rectangle([40, 30, 56, 62], fill=r((96, 112, 128)))
d.polygon([(36, 30), (48, 10), (60, 30)], fill=r(PD))
d.polygon([(40, 28), (48, 12), (56, 28)], fill=r(PC))
d.polygon([(44, 10), (48, 2), (52, 10)], fill=r(PL))
d.ellipse([12, 48, 24, 60], fill=r(PC, 150))
d.ellipse([72, 50, 84, 62], fill=r(PC, 150))
sv('blocks/production', 'crystal-drill.png', im)

# borer
im = Image.new('RGBA', (128, 128), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rounded_rectangle([4, 85, 124, 124], radius=6, fill=r((64, 85, 101)), outline=r(PC, 150))
d.rectangle([52, 50, 76, 86], fill=r((85, 101, 117)))
for dx, dy in [(20, 54), (88, 54), (54, 20), (54, 88)]:
    d.polygon([(dx, dy+14), (dx+8, dy-4), (dx+16, dy+14)], fill=r(PC))
    d.polygon([(dx+4, dy+10), (dx+8, dy), (dx+12, dy+10)], fill=r(PL))
d.ellipse([52, 52, 76, 76], fill=r(PC, 200))
d.ellipse([58, 58, 70, 70], fill=r(PL))
sv('blocks/production', 'crystal-borer.png', im)

# fluid mixer
im = Image.new('RGBA', (96, 96), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rounded_rectangle([4, 16, 92, 92], radius=6, fill=r((48, 62, 72)), outline=r(PC, 180))
d.ellipse([14, 30, 82, 82], fill=r(PC, 100))
d.polygon([(48, 2), (48, 20), (40, 14), (48, 20), (56, 14)], fill=r((96, 112, 128)), outline=r(PC, 150))
d.ellipse([44, 4, 52, 12], fill=r(PD))
for bx, by in [(20, 50), (30, 70), (60, 60), (70, 45)]:
    d.ellipse([bx, by, bx+8, by+8], fill=r(PL, 60))
sv('blocks/production', 'crystal-fluid-mixer.png', im)

# drill rim
im = Image.new('RGBA', (96, 96), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rounded_rectangle([1, 1, 94, 94], radius=3, fill=r((69, 85, 96)), outline=r(PC, 200))
d.ellipse([8, 8, 88, 88], fill=r(PD), outline=r(PL, 180))
d.ellipse([20, 20, 76, 76], fill=None, outline=r(PC, 100))
for i in range(8):
    a = math.radians(i * 45)
    d.line([(48+34*math.cos(a), 48+34*math.sin(a)),
            (48+40*math.cos(a), 48+40*math.sin(a))], fill=r(PL, 200), width=2)
sv('blocks/production', 'crystal-drill-rim.png', im)

# drill rotator
im = Image.new('RGBA', (48, 48), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.ellipse([4, 4, 44, 44], fill=r(PD), outline=r(PL, 200))
d.ellipse([12, 12, 36, 36], fill=r(PC), outline=r(PC))
d.ellipse([20, 20, 28, 28], fill=r(PL))
sv('blocks/production', 'crystal-drill-rotator.png', im)

# drill top
im = Image.new('RGBA', (96, 24), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rounded_rectangle([0, 0, 95, 23], radius=3, fill=r((12, 20, 30)), outline=r(PC, 180))
for i in range(8):
    x = 4 + i * 12
    d.polygon([(x, 4), (x+7, 3), (x+4, 19)], fill=r(PC, 80))
sv('blocks/production', 'crystal-drill-top.png', im)

# borer rim
im = Image.new('RGBA', (128, 128), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rounded_rectangle([1, 1, 126, 126], radius=3, fill=r((69, 85, 96)), outline=r(PC, 200))
d.ellipse([8, 8, 120, 120], fill=r(PD), outline=r(PL, 180))
d.ellipse([24, 24, 104, 104], fill=None, outline=r(PC, 100))
for i in range(12):
    a = math.radians(i * 30)
    d.line([(64+49*math.cos(a), 64+49*math.sin(a)),
            (64+56*math.cos(a), 64+56*math.sin(a))], fill=r(PL, 180), width=2)
sv('blocks/production', 'crystal-borer-rim.png', im)

# borer rotator
im = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.ellipse([4, 4, 60, 60], fill=r(PD), outline=r(PL, 200))
d.ellipse([14, 14, 50, 50], fill=r(PC), outline=r(PC))
d.ellipse([26, 26, 38, 38], fill=r(PL))
sv('blocks/production', 'crystal-borer-rotator.png', im)

# borer top
im = Image.new('RGBA', (128, 24), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rounded_rectangle([0, 0, 127, 23], radius=3, fill=r((12, 20, 30)), outline=r(PC, 180))
for i in range(10):
    x = 4 + i * 12
    d.polygon([(x, 3), (x+8, 3), (x+4, 18)], fill=r(PC, 80))
sv('blocks/production', 'crystal-borer-top.png', im)

# borer top invert
im = Image.new('RGBA', (128, 24), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rounded_rectangle([0, 0, 127, 23], radius=3, fill=r((10, 18, 28)), outline=r(PC, 180))
for i in range(10):
    x = 4 + i * 12
    d.polygon([(x, 20), (x+8, 20), (x+4, 5)], fill=r(PC, 60))
sv('blocks/production', 'crystal-borer-top-invert.png', im)

# borer glow
im = Image.new('RGBA', (128, 128), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.ellipse([10, 10, 118, 118], fill=r(PL, 40))
d.ellipse([30, 30, 98, 98], fill=r(PC, 25))
sv('blocks/production', 'crystal-borer-glow.png', im)

# borer arrow
im = Image.new('RGBA', (40, 40), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.polygon([(20, 2), (38, 20), (20, 38), (2, 20)], fill=r(PC), outline=r(PL, 180))
d.polygon([(20, 8), (30, 20), (20, 32), (10, 20)], fill=r(PD))
sv('blocks/production', 'crystal-borer-arrow.png', im)

# borer arrow blur
im = Image.new('RGBA', (40, 40), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.polygon([(20, 4), (36, 20), (20, 36)], fill=r(PC, 120))
d.polygon([(20, 10), (28, 20), (20, 30)], fill=r(PL, 80))
sv('blocks/production', 'crystal-borer-arrow-blur.png', im)

# conduit tops
for i in range(5):
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    if i == 0:
        d.rectangle([0, 10, 31, 22], fill=r(PD), outline=r(PC, 180))
        d.ellipse([12, 12, 20, 20], fill=r(PC, 150))
    elif i == 1:
        d.pieslice([-10, -10, 20, 20], 0, 90, fill=r(PD), outline=r(PC, 180))
        d.rectangle([10, 0, 22, 10], fill=r(PD))
        d.rectangle([0, 10, 10, 22], fill=r(PD))
        d.ellipse([12, 12, 20, 20], fill=r(PC, 150))
    elif i == 2:
        d.rectangle([0, 10, 31, 22], fill=r(PD))
        d.rectangle([10, 0, 22, 10], fill=r(PD))
        d.ellipse([12, 12, 20, 20], fill=r(PC, 150))
    elif i == 3:
        d.rectangle([0, 10, 31, 22], fill=r(PD))
        d.rectangle([10, 0, 22, 31], fill=r(PD))
        d.ellipse([12, 12, 20, 20], fill=r(PC, 150))
    else:
        d.rectangle([2, 8, 29, 24], fill=r(PD), outline=r(PC, 200))
        d.ellipse([8, 10, 24, 22], fill=r(PC, 150))
    sv('blocks/transport', f'crystal-conduit-top-{i}.png', im)

# conduit cap
im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rounded_rectangle([2, 2, 29, 29], radius=4, fill=r(PD), outline=r(PC, 200))
d.ellipse([8, 8, 24, 24], fill=r(PC))
d.ellipse([12, 12, 20, 20], fill=r(PL))
sv('blocks/transport', 'crystal-conduit-cap.png', im)

# conduit bottoms
for i in range(5):
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    if i == 0:
        d.rectangle([0, 10, 31, 22], fill=r((40, 56, 64)), outline=r(PC, 80))
    elif i == 1:
        d.pieslice([-10, -10, 20, 20], 0, 90, fill=r((40, 56, 64)), outline=r(PC, 80))
        d.rectangle([10, 0, 22, 10], fill=r((40, 56, 64)))
        d.rectangle([0, 10, 10, 22], fill=r((40, 56, 64)))
    elif i == 2:
        d.rectangle([0, 10, 31, 22], fill=r((40, 56, 64)))
        d.rectangle([10, 0, 22, 10], fill=r((40, 56, 64)))
    elif i == 3:
        d.rectangle([0, 10, 31, 22], fill=r((40, 56, 64)))
        d.rectangle([10, 0, 22, 31], fill=r((40, 56, 64)))
    else:
        d.rectangle([2, 8, 29, 24], fill=r((40, 56, 64)), outline=r(PC, 100))
    sv('blocks/transport', f'crystal-conduit-bottom-{i}.png', im)

# battery
im = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rounded_rectangle([2, 2, 61, 61], radius=6, fill=r(PD), outline=r(PC, 200))
d.rounded_rectangle([6, 6, 57, 57], radius=4, fill=r((0x2C, 0x5C, 0x9A)), outline=r(PC, 100))
d.line([(10, 20), (10, 44)], fill=r(PC, 180), width=3)
d.line([(14, 28), (14, 44)], fill=r(PL, 120), width=2)
d.line([(18, 22), (18, 44)], fill=r(PC, 140), width=2)
d.line([(22, 30), (22, 44)], fill=r(PL, 100), width=2)
for i in range(6):
    dx = 32 + i * 4
    d.rectangle([dx, 44 - i * 4, dx + 2, 44], fill=r(PC, 160 - i * 20))
d.rectangle([8, 2, 14, 6], fill=r(PD), outline=r(PC, 150))
d.rectangle([50, 2, 56, 6], fill=r(PD), outline=r(PC, 150))
sv('blocks/power', 'crystal-battery.png', im)

# battery top
im = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.ellipse([14, 14, 50, 50], fill=r(PC, 60))
d.ellipse([22, 22, 42, 42], fill=r(PL, 40))
d.ellipse([28, 28, 36, 36], fill=r(PC, 100))
for i in range(4):
    a = math.radians(i * 90 + 45)
    dx, dy = 32 + 10 * math.cos(a), 32 + 10 * math.sin(a)
    d.ellipse([dx - 2, dy - 2, dx + 2, dy + 2], fill=r(PL, 120))
sv('blocks/power', 'crystal-battery-top.png', im)

# battery power (drawPower fill)
im = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rounded_rectangle([2, 2, 61, 61], radius=6, fill=r(PC, 200))
for i in range(6):
    dx = 32 + i * 4
    d.rectangle([dx, 44 - i * 4, dx + 2, 44], fill=r(PL, 200))
sv('blocks/power', 'crystal-battery-power.png', im)

# battery glow
im = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.ellipse([12, 12, 52, 52], fill=r(PC, 60))
d.ellipse([20, 20, 44, 44], fill=r(PL, 30))
sv('blocks/power', 'crystal-battery-glow.png', im)

print("ALL DONE")