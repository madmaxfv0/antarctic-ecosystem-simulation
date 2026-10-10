import java.util.List;
import javafx.scene.paint.Color;

/**
 * Represents phytoplankton — the base of the Antarctic food web.
 *
 * Phytoplankton fill every cell that is not occupied by an animal.
 * They are displayed as green and are treated as "free" cells:
 * animals can move into phytoplankton cells (replacing them),
 * and newborn animals can be placed on them.
 *
 * When a prey animal moves into a phytoplankton cell it gains
 * energy equal to {@link #FOOD_VALUE}.
 *
 * Phytoplankton can slowly spread into adjacent truly-empty (null)
 * cells on each step, simulating natural algal growth in the
 * Southern Ocean.
 *
 * @author Fakhriyya Vazirova
 * @version 2025.02.23
 */
public class Phytoplankton extends Animal {

    
    public static final int FOOD_VALUE = 5;

    
    private static final double SPREAD_PROBABILITY = 0.02;

    
    public Phytoplankton(Field field, Location location) {
        super(field, location, Color.GREEN, false, new Gene());
    }

    /**
     * Phytoplankton spread into one adjacent
     * truly empty (null) cell, representing natural algal bloom.
     *
     * @param newPlants List to receive newly grown phytoplankton.
     */
    @Override
    public void act(List<Animal> newPlants) {
        if (isAlive()) {
            List<Location> freeLocations =
                getField().getTrulyFreeAdjacentLocations(getLocation());

            if (!freeLocations.isEmpty()
                    && Randomizer.getRandom().nextDouble() < SPREAD_PROBABILITY) {
                Location loc = freeLocations.get(0);
                Phytoplankton offspring = new Phytoplankton(getField(), loc);
                newPlants.add(offspring);
            }
        }
    }
}
