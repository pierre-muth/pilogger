found = false
srv = net.createConnection(net.TCP, 0)

srv:on("receive", function(sck, c) 
    print(#c) 
    index = 0
    array = {}
    for i in string.gmatch(c, "%S+") do
        if (found) then
            array[index] = encoder.fromHex(i)
            print(array[index])
            index = index +1
        else 
            if (string.find(i, "<br>data:<br>") ~= nil) then
                found = true
            end
        end
        
    end    
end)

srv:on("connection", function(sck, c)
  -- Wait for connection before sending.
  sck:send("GET /pilogger/getPNG.php?line=0 HTTP/1.1\r\nHost: muth.inc.free.fr\r\nConnection: keep-alive\r\nAccept: */*\r\n\r\n")
end)

srv:connect(80,"muth.inc.free.fr")
