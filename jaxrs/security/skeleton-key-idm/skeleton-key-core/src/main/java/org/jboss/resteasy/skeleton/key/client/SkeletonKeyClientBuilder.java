package org.jboss.resteasy.skeleton.key.client;

import org.jboss.resteasy.client.jaxrs.ProxyBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.skeleton.key.keystone.model.Access;
import org.jboss.resteasy.skeleton.key.keystone.model.Authentication;
import org.jboss.resteasy.skeleton.key.keystone.model.Mappers;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SkeletonKeyClientBuilder
{
   protected ResteasyWebTarget admin;
   protected TokenFactory tokenFactory;
   protected String username;
   protected String password;

   public SkeletonKeyClientBuilder username(String username)
   {
      this.username = username;
      return this;
   }

   public SkeletonKeyClientBuilder password(String password)
   {
      this.password = password;
      return this;
   }

   public SkeletonKeyClientBuilder idp(WebTarget uri)
   {
      admin = (ResteasyWebTarget)uri;
      WebTarget target = uri.path("tokens");
      Mappers.registerContextResolver(target.configuration());
      tokenFactory = ProxyBuilder.builder(TokenFactory.class, target).build();
      return this;
   }

   public Access authenticate(final String projectName, WebTarget target)
   {
      if (username == null) throw new NullPointerException("username is null");
      if (password == null) throw new NullPointerException("password is null");
      if (tokenFactory == null) throw new NullPointerException("idp is null");

      final Access access = obtainToken(projectName);
      ClientRequestFilter tokenFilter = new ClientRequestFilter() {
         volatile Access token = access;

         @Override
         public void filter(ClientRequestContext requestContext) throws IOException
         {
            Access tmp = token;
            if (tmp.getToken().expired())
            {
               synchronized (this)
               {
                  tmp = token;
                  if (tmp.getToken().expired())
                  {
                     token = tmp = obtainToken(projectName);
                  }
               }
            }
            requestContext.getHeaders().putSingle("X-Auth-Token", tmp.getToken().getId());
         }
      };

      target.configuration().register(tokenFilter);
      return access;
   }

   protected Access obtainToken(String projectName)
   {
      Authentication auth = authentication(projectName);
      return tokenFactory.create(auth);
   }

   public Authentication authentication(String projectName)
   {
      Authentication auth = new Authentication();
      Authentication.PasswordCredentials creds = new Authentication.PasswordCredentials();
      creds.setUsername(username);
      creds.setPassword(password);
      auth.setProjectName(projectName);
      auth.setPasswordCredentials(creds);
      return auth;
   }

   public SkeletonKeyAdminClient admin()
   {
      ResteasyWebTarget clone = admin.clone();
      Mappers.registerContextResolver(clone.configuration());
      authenticate("Skeleton Key", clone);
      return clone.proxy(SkeletonKeyAdminClient.class);
   }

}
