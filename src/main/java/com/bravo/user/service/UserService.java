package com.bravo.user.service;

import com.bravo.user.dao.model.User;
import com.bravo.user.dao.model.mapper.ResourceMapper;
import com.bravo.user.dao.repository.UserRepository;
import com.bravo.user.dao.specification.UserSpecification;
import com.bravo.user.exception.DataNotFoundException;
import com.bravo.user.model.dto.UserReadDto;
import com.bravo.user.model.dto.UserSaveDto;
import com.bravo.user.model.filter.UserFilter;
import com.bravo.user.utility.PageUtil;
import com.bravo.user.utility.ValidatorUtil;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;
  private final ResourceMapper resourceMapper;

  public UserService(UserRepository userRepository, ResourceMapper resourceMapper) {
    this.userRepository = userRepository;
    this.resourceMapper = resourceMapper;
  }

  public UserReadDto create(final UserSaveDto request){
    User user = userRepository.save(new User(request));

    LOGGER.info("created user '{}'", user.getId());
    return resourceMapper.convertUser(user);
  }

  /**
   * Retrieves a single user by ID
   * 
   * @param id ID to find
   * @return {@linkplain UserReadDto} with matching ID, if it exists
   */
  public UserReadDto retrieve(final String id){
    Optional<User> optional = userRepository.findById(id);
    User user = getUser(id, optional);

    LOGGER.info("found user '{}'", id);
    return resourceMapper.convertUser(user);
  }
  
  /**
   * Retrieves a paginated list of users using the criteria specified in the given filter.
   * 
   * @param filter {@linkplain UserFilter} Containing search criteria to filter on
   * @param pageRequest {@linkplain PageRequest} containing pagination parameters
   * @param response {@linkplain HttpServletResponse} in which to store page information
   * @return List of {@linkplain UserReadDto}s that meet the filter criteria
   */
  public List<UserReadDto> retrieve(
      final UserFilter filter,
      final PageRequest pageRequest,
      final HttpServletResponse httpResponse
  ){
    final UserSpecification specification = new UserSpecification(filter);
    final Page<User> userPage = userRepository.findAll(specification, pageRequest);
    final List<UserReadDto> users = resourceMapper.convertUsers(userPage.getContent());
    LOGGER.info("found {} user(s)", users.size());

    PageUtil.updatePageHeaders(httpResponse, userPage, pageRequest);
    return users;
  }

  public UserReadDto update(final String id, final UserSaveDto request){

    final Optional<User> optional = userRepository.findById(id);
    final User user = getUser(id, optional);

    if(ValidatorUtil.isValid(request.getFirstName())){
      user.setFirstName(request.getFirstName());
    }
    if(ValidatorUtil.isValid(request.getMiddleName())){
      user.setFirstName(request.getMiddleName());
    }
    if(ValidatorUtil.isValid(request.getLastName())){
      user.setFirstName(request.getLastName());
    }

    final User updated = userRepository.save(user);

    LOGGER.info("updated user '{}'", updated.getId());
    return resourceMapper.convertUser(updated);
  }

  public boolean delete(final String id){

    final Optional<User> optional = userRepository.findById(id);
    final User user = getUser(id, optional);

    userRepository.deleteById(user.getId());

    final boolean isDeleted = userRepository.findById(id).isEmpty();
    if(isDeleted){
      LOGGER.info("deleted user '{}'", id);
    } else {
      LOGGER.warn("failed to delete user '{}'", id);
    }
    return isDeleted;
  }

  private User getUser(final String id, final Optional<User> user){

    if(user.isEmpty()){
      final String message = String.format("user '%s' doesn't exist", id);
      LOGGER.warn(message);
      throw new DataNotFoundException(message);
    }
    return user.get();
  }
}
