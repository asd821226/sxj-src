/**
 * Copyright 2014 Nikita Koksharov, Nickolay Borbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sxj.redis.advance;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ClusterServersConfig extends
        BaseMasterSlaveServersConfig<ClusterServersConfig>
{
    
    private List<URI> nodeAddresses = new ArrayList<URI>();
    
    public ClusterServersConfig()
    {
    }
    
    ClusterServersConfig(ClusterServersConfig config)
    {
        super(config);
        setNodeAddresses(config.getNodeAddresses());
    }
    
    public ClusterServersConfig addNodeAddress(String... addresses)
    {
        for (String address : addresses)
        {
            try
            {
                nodeAddresses.add(new URI("//" + address));
            }
            catch (URISyntaxException e)
            {
                throw new IllegalArgumentException("Can't parse " + address);
            }
        }
        return this;
    }
    
    public List<URI> getNodeAddresses()
    {
        return nodeAddresses;
    }
    
    void setNodeAddresses(List<URI> nodeAddresses)
    {
        this.nodeAddresses = nodeAddresses;
    }
    
}
