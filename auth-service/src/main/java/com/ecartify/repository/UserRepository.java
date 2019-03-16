package com.ecartify.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ecartify.domain.User;

@Repository
public interface UserRepository extends CrudRepository<User, String>
{
}
