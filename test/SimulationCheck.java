import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Headless checks for the simulation model. No JavaFX window is opened, so
 * this runs in CI: it only needs javafx.graphics on the classpath for Color.
 *
 *   java SimulationCheck            run the checks, exit non-zero on failure
 *   java SimulationCheck --csv 150  print per-generation population counts
 *
 * The shared Randomizer uses a fixed seed, so every run produces the same
 * numbers.
 */
public class SimulationCheck {

    private static final int DEPTH = SimulatorView.GRID_HEIGHT;
    private static final int WIDTH = SimulatorView.GRID_WIDTH;
    private static final Class<?>[] SPECIES = {
        Krill.class, Silverfish.class, Penguin.class,
        LeopardSeal.class, Orca.class, Phytoplankton.class
    };

    private static int failures = 0;

    public static void main(String[] args) {
        if (args.length >= 1 && args[0].equals("--csv")) {
            int generations = args.length >= 2 ? Integer.parseInt(args[1]) : 150;
            printCsv(generations);
            return;
        }

        checkGeneBounds();
        checkCrossoverInheritance();
        checkSimulationInvariants();

        if (failures > 0) {
            System.out.println(failures + " check(s) FAILED");
            System.exit(1);
        }
        System.out.println("All checks passed");
    }

    /** Every decoded trait must stay inside the limits set by the brief. */
    private static void checkGeneBounds() {
        Gene mother = new Gene();
        Gene father = new Gene();
        for (int i = 0; i < 5000; i++) {
            Gene child = new Gene(mother, father);
            expect(child.toString().length() == 14, "gene is 14 digits");
            expect(between(child.getBreedingAge(), 12, 90), "breeding age 12-90");
            expect(between(child.getLifeSpan(), 10, 120), "life span 10-120");
            expect(between(child.getBreedingProbability(), 0.0, 0.50), "breeding probability 0-0.5");
            expect(between(child.getLitterSize(), 1, 12), "litter size 1-12");
            expect(between(child.getDiseaseProbability(), 0.0, 0.50), "disease probability 0-0.5");
            expect(between(child.getMetabolism(), 0.25, 1.0), "metabolism 0.25-1");
            // breed onward so mutations accumulate across generations
            if (i % 2 == 0) { mother = child; } else { father = child; }
        }
        System.out.println("gene bounds: 5000 generations of crossover + mutation stay in range");
    }

    /**
     * Crossover takes digits 0-6 from the female and 7-13 from the male, then
     * mutates each digit by at most one. The breeding-probability digits (5-6)
     * and disease-probability digits (9-10) are never raised by clamping, so
     * a child's value can sit at most 11 points above the parent it came from.
     */
    private static void checkCrossoverInheritance() {
        for (int i = 0; i < 2000; i++) {
            Gene female = new Gene();
            Gene male = new Gene();
            Gene child = new Gene(female, male);
            expect(child.getBreedingProbability() <= female.getBreedingProbability() + 0.11 + 1e-9,
                   "breeding probability is inherited from the female half");
            expect(child.getDiseaseProbability() <= male.getDiseaseProbability() + 0.11 + 1e-9,
                   "disease probability is inherited from the male half");
        }
        System.out.println("crossover: female half and male half are inherited as specified");
    }

    /** After every step the grid is full and holds only living entities. */
    private static void checkSimulationInvariants() {
        Randomizer.reset();
        Simulator simulator = new Simulator(DEPTH, WIDTH);
        for (int step = 1; step <= 100; step++) {
            simulator.simulateOneStep();
            Field field = simulator.getField();
            int occupied = 0;
            for (int row = 0; row < DEPTH; row++) {
                for (int col = 0; col < WIDTH; col++) {
                    Animal occupant = field.getObjectAt(row, col);
                    if (occupant != null) {
                        occupied++;
                        expect(occupant.isAlive(), "no dead entity is left on the grid");
                    }
                }
            }
            expect(occupied == DEPTH * WIDTH, "every cell is refilled (step " + step + ")");
        }
        expect(simulator.getStep() == 100, "step counter advances once per step");
        System.out.println("simulation: 100 steps on a " + WIDTH + "x" + DEPTH + " grid keep the grid full and consistent");
    }

    private static void printCsv(int generations) {
        Randomizer.reset();
        Simulator simulator = new Simulator(DEPTH, WIDTH);
        StringBuilder header = new StringBuilder("generation");
        for (Class<?> species : SPECIES) {
            header.append(',').append(species.getName());
        }
        System.out.println(header);
        System.out.println(row(0, count(simulator.getField())));
        for (int gen = 1; gen <= generations; gen++) {
            simulator.simulateOneStep();
            System.out.println(row(gen, count(simulator.getField())));
        }
    }

    private static Map<Class<?>, Integer> count(Field field) {
        Map<Class<?>, Integer> counts = new LinkedHashMap<>();
        for (Class<?> species : SPECIES) {
            counts.put(species, 0);
        }
        for (int row = 0; row < DEPTH; row++) {
            for (int col = 0; col < WIDTH; col++) {
                Animal occupant = field.getObjectAt(row, col);
                if (occupant != null && occupant.isAlive()) {
                    counts.merge(occupant.getClass(), 1, Integer::sum);
                }
            }
        }
        return counts;
    }

    private static String row(int generation, Map<Class<?>, Integer> counts) {
        StringBuilder line = new StringBuilder().append(generation);
        for (Class<?> species : SPECIES) {
            line.append(',').append(counts.get(species));
        }
        return line.toString();
    }

    private static boolean between(double value, double min, double max) {
        return value >= min - 1e-9 && value <= max + 1e-9;
    }

    private static void expect(boolean condition, String what) {
        if (!condition) {
            failures++;
            if (failures <= 10) {
                System.out.println("FAILED: " + what);
            }
        }
    }
}
