import java.util.List;
import java.util.Random;
import javafx.scene.paint.Color;


public abstract class Animal {

    /** Steps a diseased animal survives before death. */
    private static final int DISEASE_DURATION = 8;

    private static final Random rand = Randomizer.getRandom();

    // Core state
    private boolean alive;
    private Field field;
    private Location location;
    private Color color;

    // Genetics and identity
    private Gene gene;
    private boolean female;

    // Life-cycle state
    private int age;
    private double foodLevel;

    // Disease state
    private boolean diseased;
    private int diseaseStepsLeft;

    /**
     * Create a new animal at a location in a field.
     *
     * @param field    The field currently occupied.
     * @param location The location within the field.
     * @param col      The display colour of this animal.
     * @param female   True if this animal is female.
     * @param gene     The genetic code for this animal.
     */
    public Animal(Field field, Location location, Color col,
                  boolean female, Gene gene) {
        this.alive = true;
        this.field = field;
        this.female = female;
        this.gene = gene;
        this.age = 0;
        this.foodLevel = 10.0;
        this.diseased = false;
        this.diseaseStepsLeft = 0;
        setColor(col);
        setLocation(location);
    }

    
    abstract public void act(List<Animal> newAnimals);

    
    protected int getAge() { return age; }

    
    protected void setAge(int a) { this.age = a; }

    
    protected void incrementAge() {
        age++;
        if (age > gene.getLifeSpan()) {
            setDead();
        }
    }

    
    protected double getFoodLevel() { return foodLevel; }

    
    protected void setFoodLevel(double level) { this.foodLevel = level; }

    
    protected void incrementHunger() {
        foodLevel -= gene.getMetabolism();
        if (foodLevel <= 0) {
            setDead();
        }
    }

    
    /** @return True if currently diseased. */
    public boolean isDiseased() { return diseased; }

    
    public void infect() {
        if (!diseased) {
            diseased = true;
            diseaseStepsLeft = DISEASE_DURATION;
        }
    }

    
    protected void processDisease() {
        
        if (!diseased && rand.nextDouble() < gene.getDiseaseProbability() * 0.02) {
            infect();
        }

        if (diseased) {
            diseaseStepsLeft--;
            spreadDisease();

            if (diseaseStepsLeft <= 0) {
                setDead();
            }
        }
    }

   
    private void spreadDisease() {
        if (field == null || location == null) return;

        List<Location> adjacent = field.adjacentLocations(location);
        for (Location loc : adjacent) {
            Animal neighbour = field.getObjectAt(loc);
            if (neighbour != null && neighbour.isAlive()
                    && neighbour.getClass() == this.getClass()
                    && !neighbour.isDiseased()) {
                if (rand.nextDouble() < 0.15) {
                    neighbour.infect();
                }
            }
        }
    }

    
    /** @return True if this animal is female. */
    public boolean isFemale() { return female; }

    /** @return This animal's gene. */
    public Gene getGene() { return gene; }

    /**
     * @return True if old enough to breed (age ≥ gene's breeding age).
     */
    protected boolean canBreed() {
        return age >= gene.getBreedingAge();
    }

    /**
     * Search adjacent cells for a mate: an alive, same-species,
     * opposite-sex animal that can also breed.
     *
     * @return The mate's gene, or null if no suitable mate found.
     */
    protected Gene findMateGene() {
        if (field == null || location == null) return null;

        List<Location> adjacent = field.adjacentLocations(location);
        for (Location loc : adjacent) {
            Animal neighbour = field.getObjectAt(loc);
            if (neighbour != null && neighbour.isAlive()
                    && neighbour.getClass() == this.getClass()
                    && neighbour.isFemale() != this.female
                    && neighbour.canBreed()) {
                return neighbour.getGene();
            }
        }
        return null;
    }

    /**
     * Determine the number of offspring using the gene's breeding
     * probability and litter size.
     *
     * @return Number of births (may be zero).
     */
    protected int breed() {
        int births = 0;
        if (canBreed()
                && rand.nextDouble() <= gene.getBreedingProbability()) {
            births = rand.nextInt(gene.getLitterSize()) + 1;
        }
        return births;
    }


    /** @return True if the animal is still alive. */
    public boolean isAlive() { return alive; }

    /**
     * Mark the animal as dead and remove it from the field.
     * If this animal was diseased, its carcass infects nearby
     * animals of the same species (models how avian cholera
     * spreads through dead birds contaminating the area).
     */
    protected void setDead() {
        alive = false;
        if (location != null) {
            // Carcass infection: diseased corpse spreads to neighbours.
            if (diseased) {
                spreadFromCarcass();
            }
            field.clear(location);
            location = null;
            field = null;
        }
    }

    
    private void spreadFromCarcass() {
        List<Location> adjacent = field.adjacentLocations(location);
        for (Location loc : adjacent) {
            Animal neighbour = field.getObjectAt(loc);
            if (neighbour != null && neighbour.isAlive()
                    && neighbour.getClass() == this.getClass()
                    && !neighbour.isDiseased()) {
                if (rand.nextDouble() < 0.10) {
                    neighbour.infect();
                }
            }
        }
    }

    /** @return The animal's current location. */
    protected Location getLocation() { return location; }

    /**
     * Place the animal at the new location in the field.
     * @param newLocation The animal's new location.
     */
    protected void setLocation(Location newLocation) {
        if (location != null) {
            field.clear(location);
        }
        location = newLocation;
        field.place(this, newLocation);
    }

    /** @return The animal's field. */
    protected Field getField() { return field; }

    /** Set the display colour. */
    public void setColor(Color col) { color = col; }

    /** @return The display colour. */
    public Color getColor() { return color; }
}