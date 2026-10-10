import java.util.List;
import java.util.Random;
import javafx.scene.paint.Color;

/**
 * A model of the Adélie penguin (Pygoscelis adeliae).
 *
 * Penguins are colonial prey animals that feed on phytoplankton
 * in the Southern Ocean.  They are hunted by both
 * {@link LeopardSeal} and {@link Orca}.
 *
 * In real life, Adélie penguin colonies are particularly
 * vulnerable to avian cholera (Pasteurella multocida), which
 * spreads rapidly in their dense nesting colonies. This is
 * modelled by the disease system in the {@link Animal} base class.
 *
 * @author Fakhriyya Vazirova
 * @version 2025.02.23
 */
public class Penguin extends Animal {

    
    public static final int FOOD_VALUE = 12;

    
    private static final double INITIAL_FOOD = 8.0;

    private static final Random rand = Randomizer.getRandom();

    
    public Penguin(boolean randomAge, Field field, Location location,
                   Color col, boolean female, Gene gene) {
        super(field, location, col, female, gene);

        if (randomAge) {
            setAge(rand.nextInt(gene.getLifeSpan()));
            setFoodLevel(rand.nextDouble() * INITIAL_FOOD + 1);
        } else {
            setFoodLevel(INITIAL_FOOD);
        }
    }

    
    @Override
    public void act(List<Animal> newAnimals) {
        incrementAge();
        incrementHunger();
        processDisease();

        if (isAlive()) {
            giveBirth(newAnimals);

            Location newLocation =
                getField().getFreeAdjacentLocation(getLocation());

            if (newLocation != null) {
                eatPhytoplanktonAt(newLocation);
                setLocation(newLocation);
            } else {
                setDead();
            }
        }
    }

    
    private void eatPhytoplanktonAt(Location loc) {
        Animal occupant = getField().getObjectAt(loc);
        if (occupant instanceof Phytoplankton) {
            occupant.setDead();
            setFoodLevel(getFoodLevel() + Phytoplankton.FOOD_VALUE);
        }
    }

    
    private void giveBirth(List<Animal> newAnimals) {
        if (!isFemale()) return;
        Gene mateGene = findMateGene();
        if (mateGene == null) return;

        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();

        for (int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);

            Animal occupant = field.getObjectAt(loc);
            if (occupant instanceof Phytoplankton) {
                occupant.setDead();
            }

            Gene childGene;
            if (isFemale()) {
                childGene = new Gene(getGene(), mateGene);
            } else {
                childGene = new Gene(mateGene, getGene());
            }

            boolean childFemale = rand.nextBoolean();
            Penguin young = new Penguin(false, field, loc,
                                        getColor(), childFemale, childGene);
            newAnimals.add(young);
        }
    }
}
