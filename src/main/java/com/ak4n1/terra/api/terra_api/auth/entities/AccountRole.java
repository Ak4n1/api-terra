package com.ak4n1.terra.api.terra_api.auth.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Entidad intermedia que representa la relación Many-to-Many entre AccountMaster y Role.
 * 
 * <p>Esta entidad mapea la tabla `accounts_roles` que vincula usuarios con sus roles.
 * Aunque JPA puede manejar relaciones Many-to-Many con @JoinTable, esta entidad permite
 * acceder directamente a la tabla intermedia si es necesario.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Entity
@Table(name = "accounts_roles")
@IdClass(AccountRole.AccountRoleId.class)
public class AccountRole implements Serializable {

    @Id
    @Column(name = "account_id")
    private Long accountId;

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private AccountMaster accountMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;

    // Constructor vacío
    public AccountRole() {
    }

    // Constructor con parámetros
    public AccountRole(Long accountId, Long roleId) {
        this.accountId = accountId;
        this.roleId = roleId;
    }

    // Getters y setters
    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public AccountMaster getAccountMaster() {
        return accountMaster;
    }

    public void setAccountMaster(AccountMaster accountMaster) {
        this.accountMaster = accountMaster;
        if (accountMaster != null) {
            this.accountId = accountMaster.getId();
        }
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
        if (role != null) {
            this.roleId = role.getId();
        }
    }

    // Clase interna para la clave compuesta
    public static class AccountRoleId implements Serializable {
        private Long accountId;
        private Long roleId;

        public AccountRoleId() {
        }

        public AccountRoleId(Long accountId, Long roleId) {
            this.accountId = accountId;
            this.roleId = roleId;
        }

        public Long getAccountId() {
            return accountId;
        }

        public void setAccountId(Long accountId) {
            this.accountId = accountId;
        }

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AccountRoleId)) return false;
            AccountRoleId that = (AccountRoleId) o;
            return Objects.equals(accountId, that.accountId) &&
                   Objects.equals(roleId, that.roleId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, roleId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountRole)) return false;
        AccountRole that = (AccountRole) o;
        return Objects.equals(accountId, that.accountId) &&
               Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, roleId);
    }
}

