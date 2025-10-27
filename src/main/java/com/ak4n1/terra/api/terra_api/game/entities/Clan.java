package com.ak4n1.terra.api.terra_api.game.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;


@Entity
@Table(name = "clan_data")
@Immutable
public class Clan {

    @Id
    @Column(name = "clan_id", nullable = false)
    private Integer clanId;

    @Column(name = "clan_name", length = 45)
    private String clanName;

    @Column(name = "clan_level")
    private Integer clanLevel;

    @Column(name = "reputation_score", nullable = false)
    private Integer reputationScore = 0;

    @Column(name = "hasCastle")
    private Integer hasCastle;

    @Column(name = "blood_alliance_count", nullable = false)
    private Integer bloodAllianceCount = 0;

    @Column(name = "blood_oath_count", nullable = false)
    private Integer bloodOathCount = 0;

    @Column(name = "ally_id")
    private Integer allyId;

    @Column(name = "ally_name", length = 45)
    private String allyName;

    @Column(name = "leader_id")
    private Integer leaderId;

    @Column(name = "crest_id")
    private Integer crestId;

    @Column(name = "crest_large_id")
    private Integer crestLargeId;

    @Column(name = "ally_crest_id")
    private Integer allyCrestId;

    @Column(name = "auction_bid_at", nullable = false)
    private Integer auctionBidAt = 0;

    @Column(name = "ally_penalty_expiry_time", nullable = false)
    private Long allyPenaltyExpiryTime = 0L;

    @Column(name = "ally_penalty_type", nullable = false)
    private Byte allyPenaltyType = 0;

    @Column(name = "char_penalty_expiry_time", nullable = false)
    private Long charPenaltyExpiryTime = 0L;

    @Column(name = "dissolving_expiry_time", nullable = false)
    private Long dissolvingExpiryTime = 0L;

    @Column(name = "new_leader_id", nullable = false)
    private Long newLeaderId = 0L;


    public Integer getReputationScore() {
        return reputationScore;
    }

    public void setReputationScore(Integer reputationScore) {
        this.reputationScore = reputationScore;
    }

    public Long getNewLeaderId() {
        return newLeaderId;
    }

    public void setNewLeaderId(Long newLeaderId) {
        this.newLeaderId = newLeaderId;
    }

    public Integer getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Integer leaderId) {
        this.leaderId = leaderId;
    }

    public Integer getHasCastle() {
        return hasCastle;
    }

    public void setHasCastle(Integer hasCastle) {
        this.hasCastle = hasCastle;
    }

    public Long getDissolvingExpiryTime() {
        return dissolvingExpiryTime;
    }

    public void setDissolvingExpiryTime(Long dissolvingExpiryTime) {
        this.dissolvingExpiryTime = dissolvingExpiryTime;
    }

    public Integer getCrestLargeId() {
        return crestLargeId;
    }

    public void setCrestLargeId(Integer crestLargeId) {
        this.crestLargeId = crestLargeId;
    }

    public Integer getCrestId() {
        return crestId;
    }

    public void setCrestId(Integer crestId) {
        this.crestId = crestId;
    }

    public String getClanName() {
        return clanName;
    }

    public void setClanName(String clanName) {
        this.clanName = clanName;
    }

    public Integer getClanLevel() {
        return clanLevel;
    }

    public void setClanLevel(Integer clanLevel) {
        this.clanLevel = clanLevel;
    }

    public Integer getClanId() {
        return clanId;
    }

    public void setClanId(Integer clanId) {
        this.clanId = clanId;
    }

    public Long getCharPenaltyExpiryTime() {
        return charPenaltyExpiryTime;
    }

    public void setCharPenaltyExpiryTime(Long charPenaltyExpiryTime) {
        this.charPenaltyExpiryTime = charPenaltyExpiryTime;
    }

    public Integer getBloodOathCount() {
        return bloodOathCount;
    }

    public void setBloodOathCount(Integer bloodOathCount) {
        this.bloodOathCount = bloodOathCount;
    }

    public Integer getBloodAllianceCount() {
        return bloodAllianceCount;
    }

    public void setBloodAllianceCount(Integer bloodAllianceCount) {
        this.bloodAllianceCount = bloodAllianceCount;
    }

    public Integer getAuctionBidAt() {
        return auctionBidAt;
    }

    public void setAuctionBidAt(Integer auctionBidAt) {
        this.auctionBidAt = auctionBidAt;
    }

    public Byte getAllyPenaltyType() {
        return allyPenaltyType;
    }

    public void setAllyPenaltyType(Byte allyPenaltyType) {
        this.allyPenaltyType = allyPenaltyType;
    }

    public Long getAllyPenaltyExpiryTime() {
        return allyPenaltyExpiryTime;
    }

    public void setAllyPenaltyExpiryTime(Long allyPenaltyExpiryTime) {
        this.allyPenaltyExpiryTime = allyPenaltyExpiryTime;
    }

    public String getAllyName() {
        return allyName;
    }

    public void setAllyName(String allyName) {
        this.allyName = allyName;
    }

    public Integer getAllyId() {
        return allyId;
    }

    public void setAllyId(Integer allyId) {
        this.allyId = allyId;
    }

    public Integer getAllyCrestId() {
        return allyCrestId;
    }

    public void setAllyCrestId(Integer allyCrestId) {
        this.allyCrestId = allyCrestId;
    }
}
