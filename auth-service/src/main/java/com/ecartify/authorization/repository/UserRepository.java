package com.ecartify.authorization.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ecartify.authorization.domain.User;

@Repository
public interface UserRepository extends CrudRepository<User, String>
{
}
