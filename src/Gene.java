import java.util.Random;

/**
 * @author Fakhriyya Vazirova
 * @version 2025.02.23
 */
public class Gene {

    private static final int GENE_LENGTH = 14;
    private static final double MUTATION_RATE = 0.20;
    private static final Random rand = Randomizer.getRandom();

    private int[] digits;

    
    public Gene() {
        digits = new int[GENE_LENGTH];
        generateRandomGene();
    }

   
    public Gene(Gene femaleGene, Gene maleGene) {
        digits = new int[GENE_LENGTH];

        for (int i = 0; i < 7; i++) {
            digits[i] = femaleGene.digits[i];
        }
        for (int i = 7; i < GENE_LENGTH; i++) {
            digits[i] = maleGene.digits[i];
        }

        applyMutation();
        clampAllTraits();
    }


    
    public int getBreedingAge() {
        int raw = digits[0] * 10 + digits[1];
        return clamp(raw, 12, 90);
    }

    
    public int getLifeSpan() {
        int raw = digits[2] * 100 + digits[3] * 10 + digits[4];
        return clamp(raw, 10, 120);
    }

    
    public double getBreedingProbability() {
        int raw = digits[5] * 10 + digits[6];
        return clamp(raw, 0, 50) / 100.0;
    }

    
    public int getLitterSize() {
        int raw = digits[7] * 10 + digits[8];
        return clamp(raw, 1, 12);
    }

    
    public double getDiseaseProbability() {
        int raw = digits[9] * 10 + digits[10];
        return clamp(raw, 0, 50) / 100.0;
    }

    
    public double getMetabolism() {
        int raw = digits[11] * 100 + digits[12] * 10 + digits[13];
        return clamp(raw, 25, 100) / 100.0;
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int d : digits) {
            sb.append(d);
        }
        return sb.toString();
    }

    
    private void generateRandomGene() {
        for (int i = 0; i < GENE_LENGTH; i++) {
            digits[i] = rand.nextInt(10);
        }
        clampAllTraits();
    }

   
    private void applyMutation() {
        for (int i = 0; i < GENE_LENGTH; i++) {
            if (rand.nextDouble() < MUTATION_RATE) {
                if (rand.nextBoolean()) {
                    digits[i] = Math.min(9, digits[i] + 1);
                } else {
                    digits[i] = Math.max(0, digits[i] - 1);
                }
            }
        }
    }

    
    private void clampAllTraits() {
        // Breeding age 12–90
        int ba = digits[0] * 10 + digits[1];
        if (ba < 12) { digits[0] = 1; digits[1] = 2; }
        if (ba > 90) { digits[0] = 9; digits[1] = 0; }

        // Life span 10–120
        int ls = digits[2] * 100 + digits[3] * 10 + digits[4];
        if (ls < 10)  { digits[2] = 0; digits[3] = 1; digits[4] = 0; }
        if (ls > 120) { digits[2] = 1; digits[3] = 2; digits[4] = 0; }

        // Breeding probability 0–50
        int bp = digits[5] * 10 + digits[6];
        if (bp > 50) { digits[5] = 5; digits[6] = 0; }

        // Litter size 1–12
        int lt = digits[7] * 10 + digits[8];
        if (lt < 1)  { digits[7] = 0; digits[8] = 1; }
        if (lt > 12) { digits[7] = 1; digits[8] = 2; }

        // Disease probability 0–50
        int dp = digits[9] * 10 + digits[10];
        if (dp > 50) { digits[9] = 5; digits[10] = 0; }

        // Metabolism 25–100
        int mt = digits[11] * 100 + digits[12] * 10 + digits[13];
        if (mt < 25)  { digits[11] = 0; digits[12] = 2; digits[13] = 5; }
        if (mt > 100) { digits[11] = 1; digits[12] = 0; digits[13] = 0; }
    }

    
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
