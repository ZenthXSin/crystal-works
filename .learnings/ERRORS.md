# Errors

Command failures and integration errors.

---

## [ERR-20260528-001] PyYAML parsed GitHub Actions `on` key as boolean

**Logged**: 2026-05-28T00:38:00+08:00
**Command**: local workflow YAML sanity check
**Issue**: `yaml.safe_load()` treated unquoted `on:` as YAML 1.1 boolean, causing `KeyError: 'on'`
**Fix**: For GitHub Actions validation, use a parser compatible with GitHub/YAML 1.2 semantics or inspect the loaded key as `True`; do not treat this as workflow syntax failure

## [ERR-20260528-001] tool-missing

**Logged**: 2026-05-28T06:10:00+08:00
**Command**: `rg -n "crystalWallLarge|crystalAssembler|payload-wall" src/crystalworks/content/CWBlocks.kt`
**Result**: `rg: not found`
**Resolution**: Use `grep -RIn` in this environment unless ripgrep availability is verified first
