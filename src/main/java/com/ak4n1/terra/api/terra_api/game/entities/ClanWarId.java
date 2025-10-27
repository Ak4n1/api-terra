// Clase para la clave compuesta (ID compuesto) con Integer en vez de String
package com.ak4n1.terra.api.terra_api.game.entities;

import java.io.Serializable;
import java.util.Objects;

public class ClanWarId implements Serializable {
    private Integer clan1;
    private Integer clan2;

    public ClanWarId() {}

    public ClanWarId(Integer clan1, Integer clan2) {
        this.clan1 = clan1;
        this.clan2 = clan2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClanWarId)) return false;
        ClanWarId that = (ClanWarId) o;
        return Objects.equals(clan1, that.clan1) &&
                Objects.equals(clan2, that.clan2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clan1, clan2);
    }

    // Getters y Setters (recomendados para JPA)
    public Integer getClan1() { return clan1; }
    public void setClan1(Integer clan1) { this.clan1 = clan1; }
    public Integer getClan2() { return clan2; }
    public void setClan2(Integer clan2) { this.clan2 = clan2; }
}
