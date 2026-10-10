import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.scene.paint.Color;

/**
 * An Antarctic Peninsula marine ecosystem simulator based on a
 * rectangular field containing phytoplankton, three prey species
 * (krill, silverfish, penguin) and two predator species
 * (leopard seal, orca).
 *
 * Each simulation step:
 *   1. Every entity (phytoplankton and animal) acts.
 *   2. Dead non-plant animals are removed.
 *   3. New offspring are added.
 *   4. Any remaining empty cells are filled with phytoplankton.
 *
 * @author David J. Barnes, Michael Kölling, Jeffery Raphael & Fakhriyya Vazirova
 * @version 2025.02.23
 */
public class Simulator {

    // Creation probabilities for the initial population.
    private static final double KRILL_CREATION_PROB       = 0.06;
    private static final double SILVERFISH_CREATION_PROB  = 0.04;
    private static final double PENGUIN_CREATION_PROB     = 0.04;
    private static final double LEOPARD_SEAL_CREATION_PROB = 0.02;
    private static final double ORCA_CREATION_PROB        = 0.015;

    // Display colours for each species.
    private static final Color KRILL_COLOR        = Color.CORAL;
    private static final Color SILVERFISH_COLOR   = Color.LIGHTBLUE;
    private static final Color PENGUIN_COLOR      = Color.ROYALBLUE;
    private static final Color LEOPARD_SEAL_COLOR = Color.DARKGOLDENROD;
    private static final Color ORCA_COLOR         = Color.BLACK;

    private List<Animal> animals;
    private Field field;
    private int step;

    /**
     * Create a simulation field with the given size.
     * @param depth Depth of the field.  Must be > 0.
     * @param width Width of the field.  Must be > 0.
     */
    public Simulator(int depth, int width) {
        animals = new ArrayList<>();
        field = new Field(depth, width);
        reset();
    }

    /**
     * Run the simulation for a single step.
     */
    public void simulateOneStep() {
        step++;

        List<Animal> newAnimals = new ArrayList<>();

        // Let every entity act.
        for (Iterator<Animal> it = animals.iterator(); it.hasNext(); ) {
            Animal animal = it.next();
            animal.act(newAnimals);
            if (!animal.isAlive()) {
                it.remove();
            }
        }

        // Add all newly born/grown entities.
        animals.addAll(newAnimals);

        // Fill any empty cells with phytoplankton.
        regrowPhytoplankton();
    }

    /**
     * Reset the simulation to a starting position.
     */
    public void reset() {
        step = 0;
        animals.clear();
        populate();
    }

    /**
     * Randomly populate the field with animals.
     * Any cell not occupied by an animal becomes phytoplankton.
     */
    private void populate() {
        Random rand = Randomizer.getRandom();
        field.clear();

        for (int row = 0; row < field.getDepth(); row++) {
            for (int col = 0; col < field.getWidth(); col++) {
                double r = rand.nextDouble();
                Location location = new Location(row, col);
                double cumulative = 0.0;

                cumulative += LEOPARD_SEAL_CREATION_PROB;
                if (r < cumulative) {
                    boolean female = rand.nextBoolean();
                    LeopardSeal seal = new LeopardSeal(true, field,
                        location, LEOPARD_SEAL_COLOR, female, new Gene());
                    animals.add(seal);
                    continue;
                }

                cumulative += ORCA_CREATION_PROB;
                if (r < cumulative) {
                    boolean female = rand.nextBoolean();
                    Orca orca = new Orca(true, field, location,
                        ORCA_COLOR, female, new Gene());
                    animals.add(orca);
                    continue;
                }

                cumulative += KRILL_CREATION_PROB;
                if (r < cumulative) {
                    boolean female = rand.nextBoolean();
                    Krill krill = new Krill(true, field, location,
                        KRILL_COLOR, female, new Gene());
                    animals.add(krill);
                    continue;
                }

                cumulative += SILVERFISH_CREATION_PROB;
                if (r < cumulative) {
                    boolean female = rand.nextBoolean();
                    Silverfish fish = new Silverfish(true, field,
                        location, SILVERFISH_COLOR, female, new Gene());
                    animals.add(fish);
                    continue;
                }

                cumulative += PENGUIN_CREATION_PROB;
                if (r < cumulative) {
                    boolean female = rand.nextBoolean();
                    Penguin penguin = new Penguin(true, field, location,
                        PENGUIN_COLOR, female, new Gene());
                    animals.add(penguin);
                    continue;
                }

                // Remaining cells → phytoplankton.
                Phytoplankton plant = new Phytoplankton(field, location);
                animals.add(plant);
            }
        }
    }

    /**
     * Fill every empty cell with a new phytoplankton.
     * Called after each step so dead-animal cells are
     * replaced with vegetation, as required by the spec.
     */
    private void regrowPhytoplankton() {
        for (int row = 0; row < field.getDepth(); row++) {
            for (int col = 0; col < field.getWidth(); col++) {
                if (field.getObjectAt(row, col) == null) {
                    Location loc = new Location(row, col);
                    Phytoplankton plant = new Phytoplankton(field, loc);
                    animals.add(plant);
                }
            }
        }
    }

    /**
     * Pause for a given time.
     * @param millisec The time to pause for, in milliseconds.
     */
    public void delay(int millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException ie) {
            // wake up
        }
    }

    /** @return The simulation field. */
    public Field getField() { return field; }

    /** @return The current step number. */
    public int getStep() { return step; }
}
