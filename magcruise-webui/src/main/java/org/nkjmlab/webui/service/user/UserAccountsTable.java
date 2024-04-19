package org.nkjmlab.webui.service.user;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.common.Tuple;
import org.nkjmlab.sorm4j.util.table_def.SimpleTableWithDefinition;
import org.nkjmlab.sorm4j.util.table_def.TableDefinition;
import org.nkjmlab.util.commons.csv.CommonsCsvUtils;
import org.nkjmlab.util.jakarta.servlet.UserSession;
import org.nkjmlab.webui.service.user.UserAccount.Role;

public class UserAccountsTable extends SimpleTableWithDefinition<UserAccount> {

  public static final String TABLE_NAME = "USER_ACCOUNTS";

  public static final String CREATED_AT = "created_at";
  public static final String MODIFIED_AT = "modified_at";
  public static final String USER_ID = "user_id";
  public static final String GROUP_NAME = "group_name";
  public static final String PASSWORD = "password";
  public static final String NICKNAME = "nickname";
  public static final String ROLE = "role";
  public static final String MAIL = "mail";
  public static final String LANGUAGE = "language";
  private static final String FIRST_NAME = "first_name";
  private static final String LAST_NAME = "last_name";
  public static final String OPTIONS = "options";

  public UserAccountsTable(DataSource dataSource) {
    super(
        Sorm.create(dataSource),
        UserAccount.class,
        TableDefinition.builder(UserAccountsTable.TABLE_NAME)
            .addColumnDefinition(USER_ID, VARCHAR, PRIMARY_KEY)
            .addColumnDefinition(GROUP_NAME, VARCHAR)
            .addColumnDefinition(NICKNAME, VARCHAR)
            .addColumnDefinition(FIRST_NAME, VARCHAR)
            .addColumnDefinition(LAST_NAME, VARCHAR)
            .addColumnDefinition(CREATED_AT, TIMESTAMP)
            .addColumnDefinition(MODIFIED_AT, TIMESTAMP)
            .addColumnDefinition(PASSWORD, VARCHAR, NOT_NULL)
            .addColumnDefinition(ROLE, VARCHAR)
            .addColumnDefinition(MAIL, VARCHAR)
            .addColumnDefinition(LANGUAGE, VARCHAR)
            .addColumnDefinition(OPTIONS, VARCHAR)
            .build());
  }

  public boolean validate(String userId, String password) {
    UserAccount u = selectByPrimaryKey(userId);
    return u.validate(password);
  }

  public UserAccount findByUserIdAndGroupId(String userId, String groupId) {
    return selectOneAllEqual(Tuple.of(USER_ID, userId), Tuple.of(GROUP_NAME, groupId));
  }

  public void register(UserAccount userAccount) {
    if (exists(userAccount)) {
      throw new RuntimeException(userAccount.getUserId() + " is already registered:");
    }
    insert(userAccount);
  }

  public boolean validateWithSaltedPassword(String userId, String groupId, String password) {
    return findByUserIdAndGroupId(userId, groupId).getPassword().equals(password);
  }

  public UserAccount findByUserSession(UserSession userSession) {
    return selectByPrimaryKey(userSession.getUserId().get());
  }

  public void readAndMerge(File userListFile) {
    merge(readAsUserAccounts(userListFile).toArray(new UserAccount[0]));
  }

  public static List<UserAccount> readAsUserAccounts(File usersCsvFile) {
    List<CSVRecord> users =
        CommonsCsvUtils.readCsvRecordList(
            CSVFormat.Builder.create()
                .setHeader("GroupName", "UserId", "Password", "Username", "Role", "Language")
                .setSkipHeaderRecord(true)
                .build(),
            usersCsvFile,
            StandardCharsets.UTF_8);

    return users.stream()
        .map(
            uc -> {
              UserAccount user =
                  new UserAccount(
                      uc.get("UserId"),
                      uc.get("GroupName"),
                      DigestUtils.sha256Hex(uc.get("Password")),
                      "ja",
                      uc.get("Username"),
                      Role.valueOf(uc.get("Role")));
              user.setCreatedAt(new Timestamp(new Date().getTime()));
              return user;
            })
        .collect(Collectors.toList());
  }
}
