package org.example.util; // Define o pacote onde esta classe está localizada, para organização do código e controle de acesso.

import java.util.UUID; // Importa a classe UUID, que é usada para gerar identificadores únicos universais (UUIDs), garantindo que cada ID seja único mesmo em sistemas distribuídos.

public class IdGenerator {

    // Gera um ID no formato: prefixo_a1b2c3d4
    public static String generate(String prefix) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return prefix + "_" + uuid.substring(0, 8);
    }

    // Métodos prontos para cada entidade
    public static String forUser()       { return generate("usr"); }
    public static String forTrip()       { return generate("trp"); }
    public static String forExpense()    { return generate("exp"); }
    public static String forCategory()   { return generate("cat"); }
    public static String forSettlement() { return generate("stl"); }
}