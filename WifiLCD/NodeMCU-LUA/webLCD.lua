found = false
array = {}
line = 0
pixel = 0
index = 0

srv = net.createConnection(net.TCP, 0)
srv:on("receive", function(sck, c) 
    print("Socket reception of ", #c, "bytes, RSSI:", wifi.sta.getrssi())
    for i in string.gmatch(c, "%S+") do
        if (found) then
            array[index] = encoder.fromHex(i)
            index = index + 1
            pixel = pixel + 1
            if (pixel)%40 == 0 then
                tmr.wdclr()
                printLine(line)
                line = line + 1
                index = 0
            end
        else 
            if (string.find(i, "<br>data:<br>") ~= nil) then
                found = true
            end
        end
    end   
      
end)

srv:on("connection", function(sck, c)
    print("Socket connection ", c)
  -- Wait for connection before sending.
  sck:send("GET /pilogger/getPNG.php HTTP/1.1\r\nHost: muth.inc.free.fr\r\nConnection: keep-alive\r\nAccept: */*\r\n\r\n")
end)

srv:on("disconnection", function(sck, c)
    print("Socket disconnection ", c)
    wifi.sta.disconnect()
end)

srv:connect(80,"muth.inc.free.fr")

gpio.mode(1, gpio.OUTPUT)
gpio.mode(2, gpio.OUTPUT)
gpio.write(1, gpio.LOW)
gpio.write(2, gpio.LOW)
spi.setup(1, spi.MASTER, spi.CPOL_LOW, spi.CPHA_LOW, 8, 400, spi.HALFDUPLEX)

--if not tmr.alarm(0, 200, tmr.ALARM_AUTO, 
--  function()
--    if toggle then
--      gpio.write(1, gpio.HIGH)
--      toggle = false
--    else
--      gpio.write(1, gpio.LOW)
--      toggle = true
--    end
--  end) 
--then print("whops no timer") end

function black_line(line_nb)
  gpio.write(2, gpio.HIGH)
  spi.send(1, 128)
  spi.send(1, line_nb)
  for i=1,50 do 
    spi.send(1, bit.bnot(i))
  end
  spi.send(1, 0)
  spi.send(1, 0)
  gpio.write(2, gpio.LOW)
end

function printLine(line_nb)
  gpio.write(2, gpio.HIGH)
  spi.send(1, 128)
  spi.send(1, bit_invert(line_nb+1))
  for i=0,39 do 
    spi.send(1, array[i])
  end
  spi.send(1, 0)
  spi.send(1, 0)
  gpio.write(2, gpio.LOW)
end

function clear()
  gpio.write(2, gpio.HIGH)
  spi.send(1, 32)
  spi.send(1, 0)
  gpio.write(2, gpio.LOW)
end

function bit_invert(byte)
  inv = 0
  if (bit.isset(byte, 0)) then inv = bit.set(inv, 7) end
  if (bit.isset(byte, 1)) then inv = bit.set(inv, 6) end
  if (bit.isset(byte, 2)) then inv = bit.set(inv, 5) end
  if (bit.isset(byte, 3)) then inv = bit.set(inv, 4) end
  if (bit.isset(byte, 4)) then inv = bit.set(inv, 3) end
  if (bit.isset(byte, 5)) then inv = bit.set(inv, 2) end
  if (bit.isset(byte, 6)) then inv = bit.set(inv, 1) end
  if (bit.isset(byte, 7)) then inv = bit.set(inv, 0) end
  return inv
end
