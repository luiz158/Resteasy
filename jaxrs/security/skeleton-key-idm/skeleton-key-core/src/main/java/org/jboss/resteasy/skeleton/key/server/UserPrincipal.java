package org.jboss.resteasy.skeleton.key.server;

import org.jboss.resteasy.skeleton.key.keystone.model.Access;

import java.security.Principal;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserPrincipal implements Principal
{
   protected Access.User user;

   public UserPrincipal(Access.User user)
   {
      this.user = user;
   }

   @Override
   public String getName()
   {
      return user.getUsername();
   }

   public Access.User getUser()
   {
      return user;
   }
}
