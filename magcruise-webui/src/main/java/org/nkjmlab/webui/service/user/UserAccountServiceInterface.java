package org.nkjmlab.webui.service.user;

public interface UserAccountServiceInterface {

  boolean login(String userId, String password);

  boolean logout();

  /** register and login */
  boolean signup(UserAccount account);

  boolean register(UserAccount account);

  boolean merge(UserAccount account);

  boolean update(UserAccount account);

  public boolean delete(String userId);

  boolean updatePassword(String userId, String oldPassword, String newPassword);

  boolean updatePasswordByAdmin(String userId, String newPassword);

  boolean exists(String userId);

  public boolean uploadUsersCsv(String base64EncodedFile);
}
