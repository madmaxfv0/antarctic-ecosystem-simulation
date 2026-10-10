# Design notes

Derived from the source in `src/` and from the report submitted with the coursework.

## Species

| Species | Role | Colour | Food value when eaten | Eats |
|---|---|---|---|---|
| Phytoplankton | Plant | Green | 5 | — (spreads) |
| Antarctic krill | Prey | Coral | 7 | Phytoplankton |
| Antarctic silverfish | Prey | Light blue | 9 | Phytoplankton |
| Adélie penguin | Prey | Royal blue | 12 | Phytoplankton |
| Leopard seal | Predator | Dark goldenrod | — | Krill, silverfish, penguin |
| Orca | Predator | Black | — | Krill, silverfish, penguin |

Initial placement is random per cell: leopard seal 2 %, orca 1.5 %, krill 6 %, silverfish 4 %, penguin 4 %; every other cell starts as phytoplankton (`Simulator.populate`).

## One simulation step

`Simulator.simulateOneStep`:

1. Every entity's `act` is called once. Entities that die during the step are removed from the list.
2. Offspring created during the step are added.
3. Every empty cell is refilled with phytoplankton, so a dead animal is always replaced by a plant.

An animal's `act` runs in this order: age by one (dies past its life span), lose `metabolism` units of food (dies at zero), progress disease, then, if still alive, try to breed, look for food in adjacent cells, and move.

## Gene format

Fourteen decimal digits. Example: `15086350812080`.

| Digits | Trait | Range | Example value |
|---|---|---|---|
| 0–1 | Breeding age | 12–90 | 15 |
| 2–4 | Life span | 10–120 | 86 |
| 5–6 | Breeding probability | 0.00–0.50 | 0.35 |
| 7–8 | Litter size | 1–12 | 8 |
| 9–10 | Disease probability | 0.00–0.50 | 0.12 |
| 11–13 | Metabolism | 0.25–1.00 | 0.80 |

- **First generation:** digits are random, then clamped so each decoded trait is in range.
- **Crossover:** digits 0–6 from the female parent, 7–13 from the male parent.
- **Mutation:** each digit independently has a 20 % chance to change; half the time +1, half the time −1, held within 0–9.
- **Clamping:** applied again after mutation.

## Breeding

Only females give birth. A female needs an adjacent, living, opposite-sex animal of the same species that has reached its own breeding age (`Animal.findMateGene`). If she has reached breeding age and a random draw falls within her breeding probability, she produces between one and `litterSize` young, limited by the free adjacent cells. Each newborn is male or female with equal probability.

## Disease

A boolean `diseased` flag on each animal, with a countdown.

| Route | Rule |
|---|---|
| Spontaneous | Each step, a healthy animal becomes infected with probability `diseaseProbability × 0.02`. |
| Contact | Each step, an infected animal infects each adjacent healthy animal of the same species with probability 0.15. |
| Carcass | When an infected animal dies, each adjacent healthy animal of the same species is infected with probability 0.10. |

An infected animal keeps acting for 8 steps, then dies. Because the disease probability is part of the gene, susceptibility is itself heritable.

## Reproducibility

`Randomizer` hands out one shared `java.util.Random` seeded with `1111`. `SimulationCheck --csv N` resets it before the run, so the exported counts are identical on every machine. The file `docs/evidence/population-seed-1111.csv` is that export for 150 generations.
