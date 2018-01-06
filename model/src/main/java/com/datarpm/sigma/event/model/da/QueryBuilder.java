/*******************************************************************************
 * Copyright 2017 DataRPM
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.datarpm.sigma.event.model.da;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import com.datarpm.sigma.event.model.PersistencePropertyProvider;

public class QueryBuilder<E> {
  protected TransactionManager tm;
  protected EntityManagerFactory emf;
  private Class<E> entityClass;

  public QueryBuilder(Class<E> entityClass) {
    this.entityClass = entityClass;
    tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
    Map<String, String> properties = new PersistencePropertyProvider().preparePersistenceConfig();
    properties.put("hibernate.search.default.directory_provider", "filesystem");
    String indexPath = "./cache/index/";
    if (System.getenv("SIGMA_EVENTENGINE_HOME") != null) {
      indexPath = System.getenv("SIGMA_EVENTENGINE_HOME") + "/cache/index/";
    }
    properties.put("hibernate.search.default.indexBase", indexPath);
    emf = Persistence.createEntityManagerFactory("event-engine-jpa", properties);
  }

  public E create(E entity) throws NotSupportedException, SystemException {
    tm.begin();
    try {
      EntityManager em = emf.createEntityManager();
      em.persist(entity);
      em.flush();
      em.close();
      tm.commit();
    } catch (Exception e) {
      e.printStackTrace();
      throw new SystemException(e.getMessage());
    }
    return entity;
  }

  public E findById(Object id) throws NotSupportedException, SystemException {
    tm.begin();
    try {
      EntityManager em = emf.createEntityManager();
      E entity = em.find(entityClass, id);
      tm.commit();
      return entity;
    } catch (Exception e) {
      e.printStackTrace();
      throw new SystemException(e.getMessage());
    }
  }

  public void deleteById(Object id) throws NotSupportedException, SystemException {
    tm.begin();
    try {
      EntityManager em = emf.createEntityManager();
      E entity = em.find(entityClass, id);
      if (entity != null) {
        em.remove(entity);
      }
      em.flush();
      em.close();
      tm.commit();
    } catch (Exception e) {
      e.printStackTrace();
      throw new SystemException(e.getMessage());
    }
  }

  public void delete(E entity) throws NotSupportedException, SystemException {
    tm.begin();
    try {
      EntityManager em = emf.createEntityManager();
      E updatedEntity = em.merge(entity);
      em.remove(updatedEntity);
      em.flush();
      em.close();
      tm.commit();
    } catch (Exception e) {
      e.printStackTrace();
      throw new SystemException(e.getMessage());
    }
  }

  public void update(E entity) throws NotSupportedException, SystemException {
    tm.begin();
    try {
      EntityManager em = emf.createEntityManager();
      em.merge(entity);
      em.flush();
      em.close();
      tm.commit();
    } catch (Exception e) {
      e.printStackTrace();
      throw new SystemException(e.getMessage());
    }
  }

}
