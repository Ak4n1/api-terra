package com.ak4n1.terra.api.terra_api.game.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "characters")
@Immutable
public class Character {

    @Id
    @Column(name = "charId")
    private Integer charId;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "char_name")
    private String charName;

    @Column(name = "level")
    private Integer level;

    @Column(name = "maxHp")
    private Integer maxHp;

    @Column(name = "curHp")
    private Integer curHp;

    @Column(name = "maxCp")
    private Integer maxCp;

    @Column(name = "curCp")
    private Integer curCp;

    @Column(name = "maxMp")
    private Integer maxMp;

    @Column(name = "curMp")
    private Integer curMp;

    @Column(name = "face")
    private Integer face;

    @Column(name = "hairStyle")
    private Integer hairStyle;

    @Column(name = "hairColor")
    private Integer hairColor;

    @Column(name = "sex")
    private Integer sex;

    @Column(name = "heading")
    private Integer heading;

    @Column(name = "x")
    private Integer x;

    @Column(name = "y")
    private Integer y;

    @Column(name = "z")
    private Integer z;

    @Column(name = "exp")
    private Long exp;

    @Column(name = "expBeforeDeath")
    private Long expBeforeDeath;

    @Column(name = "sp")
    private Long sp;

    @Column(name = "reputation")
    private Integer reputation;

    @Column(name = "fame")
    private Integer fame;

    @Column(name = "raidbossPoints")
    private Integer raidbossPoints;

    @Column(name = "pvpkills")
    private Integer pvpkills;

    @Column(name = "pkkills")
    private Integer pkkills;

    @Column(name = "clanid")
    private Integer clanid;

    @Column(name = "race")
    private Integer race;

    @Column(name = "classid")
    private Integer classid;

    @Column(name = "base_class")
    private Integer baseClass;

    @Column(name = "transform_id")
    private Integer transformId;

    @Column(name = "deletetime")
    private Long deleteTime;

    @Column(name = "cancraft")
    private Integer canCraft;

    @Column(name = "title")
    private String title;

    @Column(name = "title_color")
    private Integer titleColor;

    @Column(name = "accesslevel")
    private Integer accessLevel;

    @Column(name = "online")
    private Integer online;

    @Column(name = "onlinetime")
    private Integer onlineTime;

    @Column(name = "char_slot")
    private Integer charSlot;

    @Column(name = "lastAccess")
    private Long lastAccess;

    @Column(name = "clan_privs")
    private Integer clanPrivs;

    @Column(name = "wantspeace")
    private Integer wantsPeace;

    @Column(name = "power_grade")
    private Integer powerGrade;

    @Column(name = "nobless")
    private Integer nobless;

    @Column(name = "subpledge")
    private Integer subPledge;

    @Column(name = "lvl_joined_academy")
    private Integer lvlJoinedAcademy;

    @Column(name = "apprentice")
    private Integer apprentice;

    @Column(name = "sponsor")
    private Integer sponsor;

    @Column(name = "clan_join_expiry_time")
    private Long clanJoinExpiryTime;

    @Column(name = "clan_create_expiry_time")
    private Long clanCreateExpiryTime;

    @Column(name = "bookmarkslot")
    private Integer bookmarkSlot;

    @Column(name = "vitality_points")
    private Integer vitalityPoints;

    @Column(name = "createDate")
    private Date createDate;

    @Column(name = "language")
    private String language;

    @Column(name = "faction")
    private Integer faction;

    @Column(name = "pccafe_points")
    private Integer pcCafePoints;

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public String getAccountName() {
        return accountName;
    }

    public Integer getApprentice() {
        return apprentice;
    }

    public Integer getBaseClass() {
        return baseClass;
    }

    public Integer getBookmarkSlot() {
        return bookmarkSlot;
    }

    public Integer getCanCraft() {
        return canCraft;
    }

    public Integer getCharId() {
        return charId;
    }

    public String getCharName() {
        return charName;
    }

    public Integer getCharSlot() {
        return charSlot;
    }

    public Long getClanCreateExpiryTime() {
        return clanCreateExpiryTime;
    }

    public Integer getClanid() {
        return clanid;
    }

    public Long getClanJoinExpiryTime() {
        return clanJoinExpiryTime;
    }

    public Integer getClanPrivs() {
        return clanPrivs;
    }

    public Integer getClassid() {
        return classid;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Integer getCurCp() {
        return curCp;
    }

    public Integer getCurHp() {
        return curHp;
    }

    public Integer getCurMp() {
        return curMp;
    }

    public Long getDeleteTime() {
        return deleteTime;
    }

    public Long getExp() {
        return exp;
    }

    public Long getExpBeforeDeath() {
        return expBeforeDeath;
    }

    public Integer getFace() {
        return face;
    }

    public Integer getFaction() {
        return faction;
    }

    public Integer getFame() {
        return fame;
    }

    public Integer getHairColor() {
        return hairColor;
    }

    public Integer getHairStyle() {
        return hairStyle;
    }

    public Integer getHeading() {
        return heading;
    }

    public String getLanguage() {
        return language;
    }

    public Long getLastAccess() {
        return lastAccess;
    }

    public Integer getLevel() {
        return level;
    }

    public Integer getLvlJoinedAcademy() {
        return lvlJoinedAcademy;
    }

    public Integer getMaxCp() {
        return maxCp;
    }

    public Integer getMaxHp() {
        return maxHp;
    }

    public Integer getMaxMp() {
        return maxMp;
    }

    public Integer getNobless() {
        return nobless;
    }

    public Integer getOnline() {
        return online;
    }

    public Integer getOnlineTime() {
        return onlineTime;
    }

    public Integer getPcCafePoints() {
        return pcCafePoints;
    }

    public Integer getPkkills() {
        return pkkills;
    }

    public Integer getPowerGrade() {
        return powerGrade;
    }

    public Integer getPvpkills() {
        return pvpkills;
    }

    public Integer getRace() {
        return race;
    }

    public Integer getRaidbossPoints() {
        return raidbossPoints;
    }

    public Integer getReputation() {
        return reputation;
    }

    public Integer getSex() {
        return sex;
    }

    public Long getSp() {
        return sp;
    }

    public Integer getSponsor() {
        return sponsor;
    }

    public Integer getSubPledge() {
        return subPledge;
    }

    public String getTitle() {
        return title;
    }

    public Integer getTitleColor() {
        return titleColor;
    }

    public Integer getTransformId() {
        return transformId;
    }

    public Integer getVitalityPoints() {
        return vitalityPoints;
    }

    public Integer getWantsPeace() {
        return wantsPeace;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public Integer getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Character character = (Character) o;
        return Objects.equals(charId, character.charId) && Objects.equals(accountName, character.accountName) && Objects.equals(charName, character.charName) && Objects.equals(level, character.level) && Objects.equals(maxHp, character.maxHp) && Objects.equals(curHp, character.curHp) && Objects.equals(maxCp, character.maxCp) && Objects.equals(curCp, character.curCp) && Objects.equals(maxMp, character.maxMp) && Objects.equals(curMp, character.curMp) && Objects.equals(face, character.face) && Objects.equals(hairStyle, character.hairStyle) && Objects.equals(hairColor, character.hairColor) && Objects.equals(sex, character.sex) && Objects.equals(heading, character.heading) && Objects.equals(x, character.x) && Objects.equals(y, character.y) && Objects.equals(z, character.z) && Objects.equals(exp, character.exp) && Objects.equals(expBeforeDeath, character.expBeforeDeath) && Objects.equals(sp, character.sp) && Objects.equals(reputation, character.reputation) && Objects.equals(fame, character.fame) && Objects.equals(raidbossPoints, character.raidbossPoints) && Objects.equals(pvpkills, character.pvpkills) && Objects.equals(pkkills, character.pkkills) && Objects.equals(clanid, character.clanid) && Objects.equals(race, character.race) && Objects.equals(classid, character.classid) && Objects.equals(baseClass, character.baseClass) && Objects.equals(transformId, character.transformId) && Objects.equals(deleteTime, character.deleteTime) && Objects.equals(canCraft, character.canCraft) && Objects.equals(title, character.title) && Objects.equals(titleColor, character.titleColor) && Objects.equals(accessLevel, character.accessLevel) && Objects.equals(online, character.online) && Objects.equals(onlineTime, character.onlineTime) && Objects.equals(charSlot, character.charSlot) && Objects.equals(lastAccess, character.lastAccess) && Objects.equals(clanPrivs, character.clanPrivs) && Objects.equals(wantsPeace, character.wantsPeace) && Objects.equals(powerGrade, character.powerGrade) && Objects.equals(nobless, character.nobless) && Objects.equals(subPledge, character.subPledge) && Objects.equals(lvlJoinedAcademy, character.lvlJoinedAcademy) && Objects.equals(apprentice, character.apprentice) && Objects.equals(sponsor, character.sponsor) && Objects.equals(clanJoinExpiryTime, character.clanJoinExpiryTime) && Objects.equals(clanCreateExpiryTime, character.clanCreateExpiryTime) && Objects.equals(bookmarkSlot, character.bookmarkSlot) && Objects.equals(vitalityPoints, character.vitalityPoints) && Objects.equals(createDate, character.createDate) && Objects.equals(language, character.language) && Objects.equals(faction, character.faction) && Objects.equals(pcCafePoints, character.pcCafePoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(charId, accountName, charName, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, face, hairStyle, hairColor, sex, heading, x, y, z, exp, expBeforeDeath, sp, reputation, fame, raidbossPoints, pvpkills, pkkills, clanid, race, classid, baseClass, transformId, deleteTime, canCraft, title, titleColor, accessLevel, online, onlineTime, charSlot, lastAccess, clanPrivs, wantsPeace, powerGrade, nobless, subPledge, lvlJoinedAcademy, apprentice, sponsor, clanJoinExpiryTime, clanCreateExpiryTime, bookmarkSlot, vitalityPoints, createDate, language, faction, pcCafePoints);
    }
}