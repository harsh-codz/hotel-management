package com.hotel.admin.app.repository;

import com.hotel.admin.app.entity.*; // User, Role
import com.hotel.admin.app.entity.enums.UserStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // Import Sort
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<User> findWithDynamicQuery(
            String username, String email, UserStatus status, Set<String> roleNames,
            Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> user = query.from(User.class);
        // Define potential joins (only perform join if needed for filtering/sorting)
        Join<User, Role> roleJoin = null; // Initialize as null

        List<Predicate> predicates = new ArrayList<>();

        if (username != null && !username.isBlank()) {
            predicates.add(cb.like(cb.lower(user.get("username")), "%" + username.toLowerCase() + "%"));
        }
        if (email != null && !email.isBlank()) {
            predicates.add(cb.like(cb.lower(user.get("email")), "%" + email.toLowerCase() + "%"));
        }
        if (status != null) {
            predicates.add(cb.equal(user.get("status"), status));
        }
         // --- Role Filtering ---
        if (roleNames != null && !roleNames.isEmpty()) {
            // Perform the join only if filtering by roles
            if (roleJoin == null) {
                roleJoin = user.join("roles", JoinType.LEFT); // Use LEFT to include users even if no roles match? Or INNER?
            }
             // Convert role names (String) to ERole enum type for comparison
             // Assuming Role entity has field 'name' of type ERole
             CriteriaBuilder.In<Role.ERole> roleInClause = cb.in(roleJoin.get("name"));
             roleNames.stream()
                     .map(name -> { // Safely map String to ERole
                         try { return Role.ERole.valueOf(name.toUpperCase()); }
                         catch (IllegalArgumentException e) { return null; } // Handle invalid role names
                     })
                     .filter(java.util.Objects::nonNull) // Filter out invalid ones
                     .forEach(roleInClause::value);
             predicates.add(roleInClause);
             query.distinct(true); // Use distinct because user can have multiple roles matching the criteria
        }
        // --- End Role Filtering ---

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        // --- Sorting ---
         List<Order> orders = new ArrayList<>();
         if (pageable.getSort().isSorted()) {
             for (Sort.Order order : pageable.getSort()) {
                 try {
                      // Handle potential sorting by role name - requires join
                      if ("roles.name".equals(order.getProperty())) { // Example handling role sort
                         if (roleJoin == null) roleJoin = user.join("roles", JoinType.LEFT);
                         orders.add(order.isAscending() ? cb.asc(roleJoin.get("name")) : cb.desc(roleJoin.get("name")));
                         query.distinct(true); // Ensure distinct if sorting by multi-valued association
                      } else {
                         // Handle direct properties
                         Path<?> sortPath = parseUserPath(user, order.getProperty()); // Simple helper
                         orders.add(order.isAscending() ? cb.asc(sortPath) : cb.desc(sortPath));
                      }
                 } catch (IllegalArgumentException e) {
                     System.err.println("Cannot sort by property: " + order.getProperty());
                 }
             }
         } else {
            // Default sort
             orders.add(cb.asc(user.get("username")));
         }
         if (!orders.isEmpty()) {
             query.orderBy(orders);
         }
         // --- End Sorting ---


        TypedQuery<User> typedQuery = em.createQuery(query);

        // --- Pagination ---
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<User> resultList = typedQuery.getResultList();

        // --- Count Query ---
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);
        Join<User, Role> countRoleJoin = null; // Re-declare join for count query if needed
         // Re-apply predicates and joins *exactly* as in the main query
         if (roleNames != null && !roleNames.isEmpty()) {
             countRoleJoin = countRoot.join("roles", JoinType.LEFT);
             // Rebuild the IN clause predicate for count query
             CriteriaBuilder.In<Role.ERole> roleInClauseCount = cb.in(countRoleJoin.get("name"));
             roleNames.stream()
                      .map(name -> { try { return Role.ERole.valueOf(name.toUpperCase()); } catch (IllegalArgumentException e) { return null; } })
                      .filter(java.util.Objects::nonNull)
                      .forEach(roleInClauseCount::value);
             predicates.set(predicates.size() - 1, roleInClauseCount); // Replace role predicate if it was the last one added
         }
         // IMPORTANT: If roles filter was added, count distinct users
         if (roleNames != null && !roleNames.isEmpty()) {
             countQuery.select(cb.countDistinct(countRoot));
         } else {
             countQuery.select(cb.count(countRoot));
         }
         countQuery.where(cb.and(predicates.toArray(new Predicate[0]))); // Reuse predicates

        Long totalCount = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, totalCount);
    }

    // Simple path parser for User entity direct fields
    private Path<?> parseUserPath(Root<User> root, String property) {
         return root.get(property); // Assumes direct properties, will fail for nested like 'roles.name'
    }
}
