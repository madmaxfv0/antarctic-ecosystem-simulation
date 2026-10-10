import java.util.HashMap;

/**
 * Collects and provides statistical data on the state of a field.
 * Dynamically creates and maintains a counter for each class of
 * animal found within the field, but excludes {@link Phytoplankton}
 * from population displays and viability checks so only true
 * animal counts are shown to the user.
 *
 * @author David J. Barnes, Michael Kölling & Fakhriyya Vazirova
 * @version 2025.02.23
 */
public class FieldStats {

    private HashMap<Class, Counter> counters;
    private boolean countsValid;

    /**
     * Construct a FieldStats object.
     */
    public FieldStats() {
        counters = new HashMap<>();
        countsValid = true;
    }

    /**
     * Get a textual summary of animal populations (excluding plants).
     * @param field The field to report on.
     * @return A string showing counts for each animal species.
     */
    public String getPopulationDetails(Field field) {
        StringBuffer buffer = new StringBuffer();
        if (!countsValid) {
            generateCounts(field);
        }
        for (Class key : counters.keySet()) {
            if (key == Phytoplankton.class) continue;
            Counter info = counters.get(key);
            buffer.append(info.getName());
            buffer.append(": ");
            buffer.append(info.getCount());
            buffer.append(' ');
        }
        return buffer.toString();
    }

    /**
     * Reset all counts to zero.
     */
    public void reset() {
        countsValid = false;
        for (Class key : counters.keySet()) {
            Counter count = counters.get(key);
            count.reset();
        }
    }

    /**
     * Increment the count for one class of animal.
     * @param animalClass The class to increment.
     */
    public void incrementCount(Class animalClass) {
        Counter count = counters.get(animalClass);
        if (count == null) {
            count = new Counter(animalClass.getName());
            counters.put(animalClass, count);
        }
        count.increment();
    }

    /**
     * Indicate that counting has been completed.
     */
    public void countFinished() {
        countsValid = true;
    }

    /**
     * Determine whether the simulation is still viable.
     * Viable = at least one non-plant animal species alive.
     */
    public boolean isViable(Field field) {
        int nonZero = 0;
        if (!countsValid) {
            generateCounts(field);
        }
        for (Class key : counters.keySet()) {
            if (key == Phytoplankton.class) continue;
            Counter info = counters.get(key);
            if (info.getCount() > 0) {
                nonZero++;
            }
        }
        return nonZero >= 1;
    }

    /**
     * Scan the entire field and count animals by species.
     */
    private void generateCounts(Field field) {
        reset();
        for (int row = 0; row < field.getDepth(); row++) {
            for (int col = 0; col < field.getWidth(); col++) {
                Animal animal = field.getObjectAt(row, col);
                if (animal != null) {
                    incrementCount(animal.getClass());
                }
            }
        }
        countsValid = true;
    }
}
