package com.acgist.killer.repository;

import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import com.acgist.killer.pojo.entity.BaseEntity;

/**
 * repository - 通用
 */
@NoRepositoryBean
@Transactional(readOnly = true)
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, String>, JpaSpecificationExecutor<T>  {
	
	/**
	 * 根据ID查询
	 * @param id ID
	 */
	default Optional<T> findId(String id) {
		return findById(id);
	}
	
	/**
	 * 根据属性查询
	 * @param name 属性名称
	 * @param value 属性值
	 */
	default Optional<T> findProperty(String name, String value) {
		return findOne(new Specification<T>() {
			private static final long serialVersionUID = 1L;
			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get(name), value);
			}
		});
	}

}
