# Bank Cleaner

A RuneLite plugin that scans your bank and identifies redundant gear — weapons and armour that are strictly outclassed by something else you already own.

## Features

- **Gear comparison** — compares items in the same equipment slot and flags anything outclassed across all combat stats (attack bonuses, defence, strength, prayer, and attack speed)
- **Weapon type awareness** — bows, crossbows, staves, and thrown weapons are compared separately and won't be flagged against each other
- **Cosmetic filtering** — items with no combat stats are ignored automatically
- **Three categories:**
  - **Sell** — tradeable items you can offload on the GE
  - **Drop / Destroy** — untradeable items taking up space
  - **Degradeables** — charged or degrading items flagged for manual review
- **Ignore list** — right-click any item to ignore it (useful for cosmetic gear or niche-use items). Toggle visibility of ignored items or clear the list entirely
- **Stat comparison tooltips** — hover an item to see a simple reason, or enable detailed tooltips in the config to see a stat-by-stat breakdown of exactly where it loses out

## Usage

1. Open your bank
2. The plugin scans automatically and populates the Bank Cleaner panel in the sidebar
3. Review the three sections and act accordingly
4. Right-click any item → **Ignore** to keep it off the list permanently

## Config Options

| Option | Default | Description |
|--------|---------|-------------|
| Show untradeables | On | Include untradeable gear in the Drop section |
| Show degradeables | On | Show degradeable items for review |
| Show ignored items | Off | Show ignored items with strikethrough instead of hiding them |
| Detailed stat tooltip | Off | Show a stat-by-stat breakdown on hover instead of just the item name |

## Notes

- The plugin only reads your bank contents — it never modifies anything
- Degradeable items (Barrows gear, Toxic blowpipe, Scythe of Vitur, etc.) are listed separately rather than compared directly, since their value depends on charges and repair costs
- The comparison is strictly stat-based — items with niche uses beyond their stats (e.g. quest requirements, cosmetic appearance) should be ignored manually

## Issues

Report bugs or suggestions at https://github.com/Jerpent/bank-cleaner-plugin/issues
