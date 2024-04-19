package org.nkjmlab.webui.service.user;

import java.io.File;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.Date;
import org.nkjmlab.util.jakarta.servlet.UserSession;
import org.nkjmlab.util.java.io.FileUtils;
import org.nkjmlab.util.java.io.SystemFileUtils;
import jakarta.servlet.http.HttpServletRequest;

public class UserAccountService implements UserAccountServiceInterface {
  private org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private UserAccountsTable userAccountsTable;
  private final HttpServletRequest request;

  public UserAccountService(UserAccountsTable userAccountsTable, HttpServletRequest request) {
    this.userAccountsTable = userAccountsTable;
    this.request = request;
  }

  @Override
  public boolean signup(UserAccount account) {
    register(account);
    login(account.getUserId(), account.getEncryptedInputPassword());
    return true;
  }

  @Override
  public boolean register(UserAccount account) {
    account.setCreatedAt(new Timestamp(new Date().getTime()));
    account.setModifiedAt(account.getCreatedAt());
    userAccountsTable.register(account);
    return false;
  }

  @Override
  public boolean update(UserAccount account) {
    if (!getUserSession().isLogined()) {
      return false;
    }
    UserAccount cua = getCurrentUserAccount();
    if (cua.isAdmin()) {
      account.setCreatedAt(new Date());
      account.setIfAbsent(getCurrentUserAccount());
      userAccountsTable.update(account);
      return true;
    } else if (cua.isSameUserAccount(account)) {
      UserAccount tmp = new UserAccount();
      tmp.setCreatedAt(new Date());
      tmp.setNickname(account.getNickname());
      tmp.setIfAbsent(getCurrentUserAccount());
      userAccountsTable.update(tmp);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean delete(String userId) {
    UserAccount u = new UserAccount();
    u.setUserId(userId);
    userAccountsTable.delete(u);
    return true;
  }

  public UserAccount getCurrentUserAccount() {
    return userAccountsTable.findByUserSession(getUserSession());
  }

  @Override
  public boolean merge(UserAccount account) {
    userAccountsTable.merge(account);
    return true;
  }

  @Override
  public boolean login(String userId, String password) {
    UserSession userSession = getUserSession();
    UserAccount userAccount = userAccountsTable.selectByPrimaryKey(userId);
    if (userAccount == null) {
      throw new RuntimeException(userId + " is not registered.");
    }
    if (!userAccountsTable.validate(userId, password)) {
      throw new RuntimeException("Password is not correct.");
    }
    userAccount.setModifiedAt(new Timestamp(new Date().getTime()));
    userAccountsTable.update(userAccount);

    userSession.setMaxInactiveInterval(10 * 60 * 60);
    userSession.setUserId(userId);
    log.info(
        "{} is logined. login session id={}",
        userSession.getSessionId(),
        userSession.getSession().getId());
    return true;
  }

  @Override
  public boolean logout() {
    getUserSession().invalidate();
    return true;
  }

  private UserSession getUserSession() {
    return UserSession.wrap(request.getSession());
  }

  @Override
  public boolean updatePasswordByAdmin(String userId, String newPassword) {
    if (getCurrentUserAccount().isAdmin()) {
      UserAccount ua = userAccountsTable.selectByPrimaryKey(userId);
      ua.setEncryptedInputPassword(newPassword);
      userAccountsTable.update(ua);
    }
    return false;
  }

  @Override
  public boolean updatePassword(String userId, String oldPassword, String newPassword) {
    if (userAccountsTable.validate(userId, oldPassword)) {
      UserAccount ua = userAccountsTable.selectByPrimaryKey(userId);
      ua.setEncryptedInputPassword(newPassword);
      userAccountsTable.update(ua);
    }
    return false;
  }

  @Override
  public boolean exists(String userId) {
    UserAccount ua = new UserAccount();
    ua.setUserId(userId);
    return userAccountsTable.exists(ua);
  }

  @Override
  public boolean uploadUsersCsv(String base64EncodedFile) {
    log.debug(base64EncodedFile);

    File outputFile =
        new File(
            SystemFileUtils.getTempDirectory(), "users-" + System.currentTimeMillis() + ".csv");

    FileUtils.write(outputFile.toPath(), base64EncodedFile, StandardOpenOption.CREATE);

    log.info("Output file is {}.", outputFile);
    return true;
  }
}
