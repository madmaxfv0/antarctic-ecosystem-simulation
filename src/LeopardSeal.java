import java.util.List;
import java.util.Iterator;
import java.util.Random;
import javafx.scene.paint.Color;

/**
 * A model of the leopard seal (Hydrurga leptonyx).
 *
 * Leopard seals are solitary apex predators of the Antarctic.
 * They hunt krill, silverfish, and penguins — the same prey
 * targeted by {@link Orca}, creating direct competition between
 * the two predator species.
 *
 * Leopard seals age, move, hunt, breed (via crossover mating),
 * contract disease, and die from old age, starvation, or illness.
 *
 * @author Fakhriyya Vazirova
 * @version 2025.02.23
 */
public class LeopardSeal extends Animal {

    
    private static final double INITIAL_FOOD = 14.0;

    private static final Random rand = Randomizer.getRandom();

    
    public LeopardSeal(boolean randomAge, Field field, Location location,
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

            
            Location newLocation = findFood();

            if (newLocation == null) {
                newLocation =
                    getField().getFreeAdjacentLocation(getLocation());
            }

            if (newLocation != null) {
                Animal occupant = getField().getObjectAt(newLocation);
                if (occupant instanceof Phytoplankton) {
                    occupant.setDead();
                }
                setLocation(newLocation);
            } else {
                setDead();  
            }
        }
    }

    
    private Location findFood() {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();

        while (it.hasNext()) {
            Location where = it.next();
            Animal animal = field.getObjectAt(where);

            if (animal instanceof Krill && animal.isAlive()) {
                animal.setDead();
                setFoodLevel(getFoodLevel() + Krill.FOOD_VALUE);
                return where;
            }
            if (animal instanceof Silverfish && animal.isAlive()) {
                animal.setDead();
                setFoodLevel(getFoodLevel() + Silverfish.FOOD_VALUE);
                return where;
            }
            if (animal instanceof Penguin && animal.isAlive()) {
                animal.setDead();
                setFoodLevel(getFoodLevel() + Penguin.FOOD_VALUE);
                return where;
            }
        }
        return null;
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
            LeopardSeal young = new LeopardSeal(false, field, loc,
                                                 getColor(), childFemale,
                                                 childGene);
            newAnimals.add(young);
        }
    }
}
