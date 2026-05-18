# Crystal Works 晶工

A small JSON mod adding crystal resource chain to Mindustry.

## Contents

- **Crystal** (item) - Blue mineral with high charge capacity
- **Crystal Alloy** (item) - Conductive alloy made from crystal
- **Crystal Fluid** (liquid) - High heat capacity coolant
- **Crystal Drill** (block) - Tier 4 drill to mine crystals
- **Crystal Smelter** (block) - Crafting station: crystal + lead → alloy

## Dev Setup

```bash
# Package for testing
cd ..
zip -r crystal-works.zip crystal-works/ -x "crystal-works/.git/*"
```

## Tech Tree

```
plastanium → crystal → crystal-alloy
                    → crystal-drill
                    → crystal-smelter
```

Version: 1.0.0
Language: Hjson (pure JSON mod)