package org.teiid.webui.backend.server.services;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.picketlink.authentication.event.PreAuthenticateEvent;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

@ApplicationScoped
public class PicketLinkDefaultUsers {

  @Inject
  private PartitionManager partitionManager;

  private boolean alreadyDone = false;

  public synchronized void create( @Observes PreAuthenticateEvent event ) {
    if ( alreadyDone ) {
      return;
    }

    alreadyDone = true;

    final IdentityManager identityManager = partitionManager.createIdentityManager();
    final RelationshipManager relationshipManager = partitionManager.createRelationshipManager();

    User admin = new User("admin");

    admin.setEmail("admin@admin.com");
    admin.setFirstName("");
    admin.setLastName("admin");

    User regular = new User("regular");

    regular.setEmail("regular@example.com");
    regular.setFirstName("Regular");
    regular.setLastName("User");

    identityManager.add(admin);
    identityManager.add(regular);
    identityManager.updateCredential(admin, new Password("admin"));
    identityManager.updateCredential(regular, new Password("123"));

    Role roleDeveloper = new Role("simple");
    Role roleAdmin = new Role("admin");

    identityManager.add(roleDeveloper);
    identityManager.add(roleAdmin);

    relationshipManager.add(new Grant(admin, roleDeveloper));
    relationshipManager.add(new Grant(admin, roleAdmin));
  }

}
