package io.oltre_backend.openfoodfacts;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class OpenFoodFactsServiceTest {

    private final OpenFoodFactsService service = new OpenFoodFactsService("https://fr.openfoodfacts.org", "test-agent");

    @Test
    void normalize_stripsAccentsAndLowercases() {
        assertEquals("pates", service.normalize("Pâtes"));
        assertEquals("pate a tartiner", service.normalize("Pâte à tartiner"));
    }

    @Test
    void nameMatchScore_prefersActualNameMatchOverUnrelatedProduct() {
        // "pate" tapé par l'utilisateur ne doit pas classer "Nutella" (dont
        // le nom ne contient pas "pate") au-dessus de vraies pâtes.
        String query = service.normalize("pate");

        int pastaScore = service.nameMatchScore(service.normalize("Pâtes Barilla"), query);
        int nutellaScore = service.nameMatchScore(service.normalize("Nutella"), query);

        assertTrue(pastaScore < nutellaScore);
    }

    @Test
    void nameMatchScore_startsWithRanksAboveContains() {
        String query = service.normalize("pate");

        int startsWith = service.nameMatchScore(service.normalize("Pâtes complètes"), query);
        int containsOnly = service.nameMatchScore(service.normalize("Sauce pour pâtes"), query);

        assertTrue(startsWith < containsOnly);
    }
}
