# ESTIMATION GUIDE — Story points, effort et profils

## 1. Principe

L'estimation combine :

1. la complexité fonctionnelle ;
2. la complexité technique ;
3. le risque ;
4. l'incertitude ;
5. le nombre de modules impactés ;
6. le niveau du développeur.

## 2. Story points

| SP | Taille | Description | Action |
|---:|---|---|---|
| 1 | XS | Très simple, connu, faible risque | Peut être pris par junior |
| 2 | S | Simple, un module, peu d'inconnues | Junior autonome ou intermédiaire |
| 3 | M | Standard, quelques impacts | Intermédiaire |
| 5 | L | Complexe, plusieurs modules | Senior ou intermédiaire encadré |
| 8 | XL | Très complexe ou incertain | Découpage recommandé |
| 13 | XXL | Trop gros | Découpage obligatoire |

## 3. Conversion indicative en effort

> Cette table sert au planning initial, elle doit être recalibrée avec les données réelles de l'équipe.

| SP | Senior | Intermédiaire | Junior autonome | Junior encadré |
|---:|---:|---:|---:|---:|
| 1 | 0.25j | 0.5j | 0.75j | 1j |
| 2 | 0.5j | 0.75j | 1j | 1.5j |
| 3 | 1j | 1.5j | 2j | 2.5j |
| 5 | 2j | 2.5-3j | 4j | 5j |
| 8 | 3-4j | 5j | 6-8j | À découper |
| 13 | À découper | À découper | À découper | À découper |

## 4. Facteurs de majoration

Ajouter une majoration si :

| Facteur | Majoration |
|---|---:|
| Code legacy non testé | +25 à +50 % |
| Impact sécurité | +20 à +40 % |
| Impact base de données/migration | +20 à +40 % |
| Multi-stack Spring + Angular + Flutter | +30 à +60 % |
| Dépendance externe non maîtrisée | +20 à +50 % |
| Spécification floue | Ne pas estimer, clarifier |
| Junior sans expérience sur module | +30 à +80 % |

## 5. Règle de découpage

Découper si :

- estimation > 3 jours ;
- story > 5 SP ;
- plus de 2 stacks impactées ;
- besoin de migration DB + UI + API ;
- incertitude forte ;
- pas de tests existants.

## 6. Format d'estimation attendu

```markdown
## Estimation

| Élément | Valeur |
|---|---|
| Story points | 3 |
| Taille | M |
| Profil recommandé | Intermédiaire |
| Effort senior | 1j |
| Effort intermédiaire | 1.5j |
| Effort junior | 2j |
| Risque | Moyen |
| Raison | Impact API + UI + tests |
```

> Standards techniques non négociables : `AGENTS.md`, section « Standards non négociables » (source unique, ne pas recopier ici).
