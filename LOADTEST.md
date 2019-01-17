## PUT

### put.lua
```lua
id = 0

wrk.method = "PUT"
wrk.body = "asdknjfknasfksfmlsalmaksvimoalsdkfaskdcmiosDMLFKASDLCKAMSDMFANSDFlmaksdfaslmkdfnasdfas4"

request = function()
	fullpath = "/v0/entity?id=" .. id
	id = id + 1
	return wrk.format(nil, fullpath)
end
```

### wrk
```
dariam:scripts dariam$ wrk --latency -c4 -t4 -d60s -s put.lua http://localhost:8080
Running 1m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     6.47ms   21.78ms 291.74ms   96.01%
    Req/Sec   387.82     89.01   640.00     79.64%
  Latency Distribution
     50%    2.41ms
     75%    2.85ms
     90%    3.48ms
     99%  127.54ms
  91536 requests in 1.00m, 8.21MB read
Requests/sec:   1523.81
Transfer/sec:    139.88KB
```

## GET

### get.lua
```lua
id = 0
wrk.method = "GET"

request = function()
	local path = "/v0/entity?id=" .. id
	id = id + 1
	return wrk.format(nil, path)
end 
```

### wrk
```
dariam:scripts dariam$ wrk --latency -c1 -t1 -d30s -s get.lua http://localhost:8080
Running 30s test @ http://localhost:8080
  1 threads and 1 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   530.84us    0.99ms  41.77ms   99.26%
    Req/Sec     2.02k   237.81     2.34k    80.00%
  Latency Distribution
     50%  452.00us
     75%  530.00us
     90%  612.00us
     99%    1.41ms
  60219 requests in 30.00s, 9.36MB read
Requests/sec:   2007.35
Transfer/sec:    319.53KB
```
```
dariam:scripts dariam$ wrk --latency -c2 -t2 -d30s -s get.lua http://localhost:8080
Running 30s test @ http://localhost:8080
  2 threads and 2 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   472.85us  214.09us   9.16ms   96.01%
    Req/Sec     2.11k   267.01     2.74k    78.24%
  Latency Distribution
     50%  441.00us
     75%  486.00us
     90%  555.00us
     99%    1.37ms
  126418 requests in 30.10s, 19.65MB read
Requests/sec:   4199.64
Transfer/sec:    668.50KB
```

## DELETE

### delete.lua
```lua
id = 0
wrk.method = "DELETE"

request = function()
	local path = "/v0/entity?id=" .. id
	id = id + 1
	return wrk.format(nil, path)
end 
```

### wrk
```
dariam:scripts dariam$ wrk --latency -c4 -t4 -d30s -s delete.lua http://localhost:8080
Running 30s test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    35.97ms  152.26ms   1.14s    94.76%
    Req/Sec     1.32k   237.36     1.51k    93.87%
  Latency Distribution
     50%  703.00us
     75%  815.00us
     90%    1.57ms
     99%  894.82ms
  148888 requests in 30.07s, 13.49MB read
Requests/sec:   4951.13
Transfer/sec:    459.34KB
```