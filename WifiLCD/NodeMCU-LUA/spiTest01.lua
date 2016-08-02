gpio.mode(1, gpio.OUTPUT)
gpio.mode(2, gpio.OUTPUT)
gpio.write(1, gpio.LOW)
gpio.write(2, gpio.LOW)
spi.setup(1, spi.MASTER, spi.CPOL_LOW, spi.CPHA_LOW, 8, 400, spi.HALFDUPLEX)

if not tmr.alarm(0, 200, tmr.ALARM_AUTO, 
  function()
    if toggle then
      gpio.write(1, gpio.HIGH)
      toggle = false
    else
      gpio.write(1, gpio.LOW)
      toggle = true
    end
  end) 
then print("whoopsie") end

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

clear()

for j=1, 240 do
  black_line(bit_invert(j))
  tmr.wdclr()
end


