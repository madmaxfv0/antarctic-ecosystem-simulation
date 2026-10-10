import java.util.List;
import java.util.Random;
import javafx.scene.paint.Color;

/**
 * A model of Antarctic krill (Euphausia superba) in the simulation.
 *
 * Krill are small crustaceans that feed on phytoplankton and 
 * they are hunted by both {@link LeopardSeal} and {@link Orca}.
 *
 * Krill age, move, eat phytoplankton, breed when a male and female
 * meet, contract avian cholera, and die from age, starvation,
 * or disease.
 *
 * @author Fakhriyya Vazirova
 * @version 2025.02.23
 */
public class Krill extends Animal {

    
    public static final int FOOD_VALUE = 7;

    
    private static final double INITIAL_FOOD = 6.0;

    private static final Random rand = Randomizer.getRandom();

    /**
     * Create a krill.
     *
     * @param randomAge If true, assign random starting age and food.
     * @param field     The field currently occupied.
     * @param location  The location within the field.
     * @param col       Display colour.
     * @param female    True if female.
     * @param gene      Genetic code.
     */
    public Krill(boolean randomAge, Field field, Location location,
                 Color col, boolean female, Gene gene) {
        super(field, location, col, female, gene);

        if (randomAge) {
            setAge(rand.nextInt(gene.getLifeSpan()));
            setFoodLevel(rand.nextDouble() * INITIAL_FOOD + 1);
        } else {
            setFoodLevel(INITIAL_FOOD);
        }
    }

    /**
     * Per-step behaviour: age, hunger, disease, breed, move + eat.
     *
     * @param newAnimals List to receive any offspring.
     */
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
            Krill young = new Krill(false, field, loc,
                                    getColor(), childFemale, childGene);
            newAnimals.add(young);
        }
    }
}
