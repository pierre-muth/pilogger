gpio.mode(1, gpio.OUTPUT)
gpio.mode(2, gpio.OUTPUT)
gpio.write(1, gpio.LOW)
gpio.write(2, gpio.LOW)
spi.setup(1, spi.MASTER, spi.CPOL_LOW, spi.CPHA_LOW, 8, 50, spi.HALFDUPLEX)

srv = net.createConnection(net.TCP, 0)

srv:on("receive", function(sck, c) 
    spi.send(1, c)
end)

srv:on("connection", function(sck, c)
    print("socket connected")
    sck:send("LCD\n")
    gpio.write(2, gpio.HIGH)
end)

srv:on("disconnection", function(sck, c)
    gpio.write(2, gpio.LOW)
    print("socket disconnected")
end)

function clear()
  gpio.write(2, gpio.HIGH)
  spi.send(1, 32)
  spi.send(1, 0)
  gpio.write(2, gpio.LOW)
end

srv:connect(9999, "192.168.1.15")
