# SKYFULL – custom enchanty & itemy (Paper 1.21.11)

Kompletní balík pro tvůj survival/utility server: **plugin** (funkční custom
enchanty a itemy) + **resource pack** (vlastní vzhled itemů).

> Důležité: samotný resource pack umí jen vzhled. Veškerá *funkčnost* je v pluginu.
> Obojí patří k sobě – nainstaluj plugin **i** resource pack.

---

## Co je uvnitř

**7 custom enchantů** (dávají se na běžné nástroje/brnění):

| Enchant | ID | Na co | Co dělá |
|---|---|---|---|
| Vein Miner | `vein_miner` | krumpáč | vytěží celou žílu rudy naráz (max lvl 3) |
| Timber | `timber` | sekera | pokácí celý strom jedním seknutím |
| Auto Smelt | `auto_smelt` | krumpáč | vytěžená ruda padá rovnou přetavená + XP navíc |
| Excavator | `excavator` | lopata | kope 3×3 (hlína, písek, štěrk…) |
| Replanter | `replanter` | motyka | při sklizni automaticky zase zaseje |
| Magnetic | `magnetic` | cokoliv | přitahuje spadlé dropy přímo do inventáře (max lvl 3) |
| Soulbound | `soulbound` | cokoliv | item ti zůstane i po smrti |

**5 custom itemů (gadgetů)** – mají vlastní texturu z resource packu:

| Item | ID | Ovládání | Co dělá |
|---|---|---|---|
| Ender Pouch | `ender_pouch` | pravý klik | otevře tvůj ender chest odkudkoliv |
| Magnet | `magnet` | shift + pravý klik = zapnout/vypnout | přitahuje dropy, když je ON |
| Green Thumb Wand | `green_thumb_wand` | pravý klik | pohnojí (bonemeal) celou plochu plodin; spotřebuje kostní moučku z inventáře |
| Warp Stone | `warp_stone` | shift+PK = nastav domov, PK = teleport domů | domov + teleport s warm-upem a cooldownem |
| Prospector's Compass | `prospectors_compass` | pravý klik | naskenuje okolí a zvýrazní rudy částicemi |

---

## Krok 1 – Vyrobit plugin `.jar`

Zdrojový kód je hotový, jen se musí zkompilovat. **Vyber si jednu z cest:**

### A) Nejjednodušší – přes GitHub (zkompiluje se v cloudu, nic neinstaluješ)
1. Vytvoř si nový (klidně soukromý) repozitář na GitHubu.
2. Nahraj do něj obsah složky `skyfull-tools` (klidně přetažením přes web –
   „Add file“ → „Upload files“). Je tam i připravený workflow
   `.github/workflows/build.yml`.
3. Otevři záložku **Actions** → počkej, až doběhne „Build SkyfullTools“
   (zelená fajfka), případně ho spusť ručně přes „Run workflow“.
4. Klikni na doběhnutý běh → dole sekce **Artifacts** → stáhni
   `SkyfullTools-jar`. Uvnitř je `SkyfullTools-1.0.0.jar`.

### B) Lokálně (pokud máš na PC Javu a Maven)
```bash
cd skyfull-tools
./build.sh          # nebo:  mvn -B package
```
Hotový jar najdeš v `target/SkyfullTools-1.0.0.jar`.

> Potřebuješ **JDK 21+** a **Maven** s přístupem k internetu.
> Java: https://adoptium.net  •  Maven: https://maven.apache.org

**Když build hlásí, že nenašel `paper-api` verzi:** otevři `pom.xml` a přepiš
řádek `<paper.version>1.21.8-R0.1-SNAPSHOT</paper.version>` na svou přesnou verzi,
např. `1.21.11-R0.1-SNAPSHOT`, a spusť build znovu. (Plugin díky
`api-version: 1.21` běží na jakékoliv 1.21.x, takže na přesné verzi tu nezáleží.)

---

## Krok 2 – Nainstalovat plugin

1. Vypni server.
2. Zkopíruj `SkyfullTools-1.0.0.jar` do složky `plugins/` na serveru.
3. Zapni server. V konzoli by mělo naskočit
   `SkyfullTools enabled - 7 enchants, 5 items ready.`
4. Konfigurace se vytvoří v `plugins/SkyfullTools/config.yml`
   (rádiusy, cooldowny, zapnutí/vypnutí jednotlivých věcí). Po úpravě dej
   `/skyfull reload`.

Vyžaduje **Paper** (nebo Purpur/jiný Paper fork). Na čistém Spigotu poběží
většina věcí, ale je doporučený Paper.

---

## Krok 3 – Nainstalovat resource pack

Soubor: **`SKYFULL-ResourcePack.zip`**

**Varianta 1 – lokálně u sebe (pro test):**
Minecraft → Nastavení → Resource Packs → „Open Pack Folder“ → nahraj tam ten zip
→ aktivuj ho. Custom itemy pak mají svůj vzhled.

**Varianta 2 – automaticky všem hráčům (doporučeno pro server):**
Nahraj `SKYFULL-ResourcePack.zip` někam s přímým odkazem (Dropbox s `?dl=1`,
vlastní web, MC-Packs.net…) a do `server.properties` dej:
```
resource-pack=https://tvuj-odkaz/SKYFULL-ResourcePack.zip
resource-pack-sha1=c9e701cb795e2d5698d0099099febf256b967792
require-resource-pack=true
```
(To SHA1 sedí na aktuální zip. Když pack změníš, přepočítej ho.)

> Bez resource packu itemy **fungují normálně**, jen vypadají jako klacík.

---

## Příkazy

| Příkaz | Co dělá |
|---|---|
| `/skyfull give <item> [hráč]` | dá custom item (např. `/skyfull give ender_pouch`) |
| `/skyfull enchant <enchant> [level]` | přidá custom enchant na item v ruce |
| `/skyfull list` | vypíše všechny enchanty a itemy |
| `/skyfull reload` | znovu načte config |

Zkratky příkazu: `/sf`, `/skytools`. Práva: `skyfull.admin` (default OP) na
give/enchant/reload; `skyfull.use` (default všichni) na používání.

Příklad: vezmi si do ruky diamantový krumpáč a dej
`/skyfull enchant vein_miner 3` → máš Vein Miner III.

---

## Crafting recepty (pro survival)

Itemy jdou i vyrobit (lze vypnout v configu `enable-recipes: false`):

```
Ender Pouch          Magnet            Green Thumb Wand
 K  E  K              .  Fe  .           .  L  .
 E  Y  E              Fe R  Fe           .  |  .
 K  E  K              .  Fe  .           .  B  .

Warp Stone           Prospector's Compass
 A  E  A              N  Su N
 E  H  A              Su C  Su
 A  E  A              N  Su N
```

Legenda: K=kůže, E=ender pearl, Y=ender oko, Fe=železný ingot, R=redstone,
L=listí (dubové), |=klacek, B=blok kostí, A=ametystový úlomek, H=echo shard,
N=zlatý nugget, Su=surové železo (raw iron), C=kompas.

---

## Rychlé tipy

- Vein Miner / Timber ubírají nástroji trvanlivost za každý blok navíc a samy se
  zastaví těsně předtím, než by se nástroj zničil.
- Magnetic (enchant) i Magnet (item) fungují stejně – rádius se dá nastavit v configu.
- Warp Stone: teleport se zruší, když se během warm-upu pohneš.
- Vše se dá vypnout jednotlivě v `config.yml`.

Hodně zábavy na **SKYFULL**! 🚀
