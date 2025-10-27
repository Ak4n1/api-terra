// Entidad ClanWar con Integer para clan1 y clan2
package com.ak4n1.terra.api.terra_api.game.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@Entity
@Table(name = "clan_wars")
@IdClass(ClanWarId.class)
@Immutable
public class ClanWar implements Serializable {

    @Id
    @Column(name = "clan1", nullable = false)
    private Integer clan1;

    @Id
    @Column(name = "clan2", nullable = false)
    private Integer clan2;

    @Column(name = "clan1Kill", nullable = false)
    private int clan1Kill;

    @Column(name = "clan2Kill", nullable = false)
    private int clan2Kill;

    @Column(name = "winnerClan", length = 35, nullable = false)
    private String winnerClan;

    @Column(name = "startTime", nullable = false)
    private long startTime;

    @Column(name = "endTime", nullable = false)
    private long endTime;

    @Column(name = "state", nullable = false)
    private byte state;

    // Constructor vacío requerido por JPA
    protected ClanWar() {}

    // Getters
    public Integer getClan1() { return clan1; }
    public Integer getClan2() { return clan2; }
    public int getClan1Kill() { return clan1Kill; }
    public int getClan2Kill() { return clan2Kill; }
    public String getWinnerClan() { return winnerClan; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public byte getState() { return state; }
}
