package com.sendish.repository;

import com.sendish.repository.model.jpa.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface CountryRepository extends JpaRepository<Country, Long> {

    Country findByIso(String iso);

}
