package com.ak4n1.terra.api.terra_api.game.dto;

import java.util.List;

public class ClanResponseDTO {

    private Integer clanId;
    private String clanName;
    private Integer clanLevel;
    private Integer reputationScore;
    private Integer hasCastle;
    private Integer bloodAllianceCount;
    private Integer bloodOathCount;
    private Integer allyId;
    private String allyName;
    private Integer leaderId;
    private Integer crestId;
    private Integer crestLargeId;
    private Integer allyCrestId;
    private Integer auctionBidAt;
    private Long allyPenaltyExpiryTime;
    private Byte allyPenaltyType;
    private Long charPenaltyExpiryTime;
    private Long dissolvingExpiryTime;
    private Long newLeaderId;
    private List<ClanMemberDTO> members;
    private Integer totalMembers;
    private String leaderName;
    private boolean hasWar;



    public boolean isHasWar() {
        return hasWar;
    }

    public void setHasWar(boolean hasWar) {
        this.hasWar = hasWar;
    }
    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }
    public Integer getAllyCrestId() {
        return allyCrestId;
    }

    public List<ClanMemberDTO> getMembers() {
        return members;
    }

    public void setMembers(List<ClanMemberDTO> members) {
        this.members = members;
    }

    public Integer getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(Integer totalMembers) {
        this.totalMembers = totalMembers;
    }

    public void setAllyCrestId(Integer allyCrestId) {
        this.allyCrestId = allyCrestId;
    }

    public Integer getAllyId() {
        return allyId;
    }

    public void setAllyId(Integer allyId) {
        this.allyId = allyId;
    }

    public String getAllyName() {
        return allyName;
    }

    public void setAllyName(String allyName) {
        this.allyName = allyName;
    }

    public Long getAllyPenaltyExpiryTime() {
        return allyPenaltyExpiryTime;
    }

    public void setAllyPenaltyExpiryTime(Long allyPenaltyExpiryTime) {
        this.allyPenaltyExpiryTime = allyPenaltyExpiryTime;
    }

    public Byte getAllyPenaltyType() {
        return allyPenaltyType;
    }

    public void setAllyPenaltyType(Byte allyPenaltyType) {
        this.allyPenaltyType = allyPenaltyType;
    }

    public Integer getAuctionBidAt() {
        return auctionBidAt;
    }

    public void setAuctionBidAt(Integer auctionBidAt) {
        this.auctionBidAt = auctionBidAt;
    }

    public Integer getBloodAllianceCount() {
        return bloodAllianceCount;
    }

    public void setBloodAllianceCount(Integer bloodAllianceCount) {
        this.bloodAllianceCount = bloodAllianceCount;
    }

    public Integer getBloodOathCount() {
        return bloodOathCount;
    }

    public void setBloodOathCount(Integer bloodOathCount) {
        this.bloodOathCount = bloodOathCount;
    }

    public Long getCharPenaltyExpiryTime() {
        return charPenaltyExpiryTime;
    }

    public void setCharPenaltyExpiryTime(Long charPenaltyExpiryTime) {
        this.charPenaltyExpiryTime = charPenaltyExpiryTime;
    }

    public Integer getClanId() {
        return clanId;
    }

    public void setClanId(Integer clanId) {
        this.clanId = clanId;
    }

    public Integer getClanLevel() {
        return clanLevel;
    }

    public void setClanLevel(Integer clanLevel) {
        this.clanLevel = clanLevel;
    }

    public String getClanName() {
        return clanName;
    }

    public void setClanName(String clanName) {
        this.clanName = clanName;
    }

    public Integer getCrestId() {
        return crestId;
    }

    public void setCrestId(Integer crestId) {
        this.crestId = crestId;
    }

    public Integer getCrestLargeId() {
        return crestLargeId;
    }

    public void setCrestLargeId(Integer crestLargeId) {
        this.crestLargeId = crestLargeId;
    }

    public Long getDissolvingExpiryTime() {
        return dissolvingExpiryTime;
    }

    public void setDissolvingExpiryTime(Long dissolvingExpiryTime) {
        this.dissolvingExpiryTime = dissolvingExpiryTime;
    }

    public Integer getHasCastle() {
        return hasCastle;
    }

    public void setHasCastle(Integer hasCastle) {
        this.hasCastle = hasCastle;
    }

    public Integer getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Integer leaderId) {
        this.leaderId = leaderId;
    }

    public Long getNewLeaderId() {
        return newLeaderId;
    }

    public void setNewLeaderId(Long newLeaderId) {
        this.newLeaderId = newLeaderId;
    }

    public Integer getReputationScore() {
        return reputationScore;
    }

    public void setReputationScore(Integer reputationScore) {
        this.reputationScore = reputationScore;
    }
}
