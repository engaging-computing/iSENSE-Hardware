import pptscanner
import pinpoint
import time
import math

import time
import numpy as np
import matplotlib
matplotlib.use('GTKAgg') # do this before importing pylab
import matplotlib.pyplot as plt
import gobject

class BaseGraph(object):

    def __init__(self, fieldNameLineTup, units, name, fig, pos, minYWindow, window = 60):
        
        self.fieldNameLineTup = fieldNameLineTup

        self.window = window
        self.slice = -window
        self.minYWindow = minYWindow

        plotArgs = []
        legendArgs = []
        self.data = []
        self.fields = []

        for field, name, line in fieldNameLineTup:
            plotArgs += [[], [], line]
            legendArgs.append(name)
            self.data.append([])
            self.fields.append(field)

        self.ax = fig.add_subplot(pos, ylabel = units, title = name, xlim = (0, window))
        self.lines = self.ax.plot(*plotArgs)
        self.legend = self.ax.legend(self.lines, tuple(legendArgs), loc = 'best')
        self.legend = self.legend.draggable()

    def animate(self, data):

        for sub, field in zip(self.data, self.fields):
            sub.append(data.dataDict[field])

        minVal = self.data[0][self.slice:][0]
        maxVal = self.data[0][self.slice:][0]

        for sub in self.data:
            for dat in sub[self.slice:]:
                if dat < minVal:
                    minVal = dat
                if dat > maxVal:
                    maxVal = dat

        if abs(maxVal - minVal) < self.minYWindow:
            
            adj = (self.minYWindow - abs(maxVal - minVal)) / 2.0
            minVal -= adj
            maxVal += adj

        dataRange = abs(maxVal - minVal)
        minVal -= dataRange * 0.1
        maxVal += dataRange * 0.1
        self.ax.set_ylim((minVal, maxVal))

        for sub, line in zip(self.data, self.lines):
            line.set_xdata(np.array(range(len(sub[self.slice:]))))
            line.set_ydata(np.array(sub[self.slice:]))

class AccelGraph(BaseGraph):

    def __init__(self, fig, pos, window = 60):
        fieldNameLineTup = (('xAccel', 'X-Axis', 'r-'),
                            ('yAccel', 'Y-Axis', 'g-'),
                            ('zAccel', 'Z-Axis', 'b-'),
                            ('accel', 'Magnitude', 'k:'))
        units = 'm/s^2'
        name = 'Acceleration'
        super(AccelGraph, self).__init__(fieldNameLineTup, units, name, fig, pos, 10.0, window)

class PressureGraph(BaseGraph):

    def __init__(self, fig, pos, window = 60):
        fieldNameLineTup = (('pressure', 'Pressure', 'r-'),)
        units = 'Pa'
        name = 'Pressure'
        super(PressureGraph, self).__init__(fieldNameLineTup, units, name, fig, pos, 1000.0, window)

class LightGraph(BaseGraph):

    def __init__(self, fig, pos, window = 60):
        fieldNameLineTup = (('light', 'Light', 'r-'),)
        units = 'lux'
        name = 'Light'
        super(LightGraph, self).__init__(fieldNameLineTup, units, name, fig, pos, 200.0, window)

class TemperatureGraph(BaseGraph):

    def __init__(self, fig, pos, window = 60):
        fieldNameLineTup = (('temperature', 'Temperature', 'r-'),)
        units = 'C'
        name = 'Temperature'
        super(TemperatureGraph, self).__init__(fieldNameLineTup, units, name, fig, pos, 10.0, window)

class HumidityGraph(BaseGraph):

    def __init__(self, fig, pos, window = 30):
        fieldNameLineTup = (('humidity', 'Humidity', 'r-'),)
        units = '%RH'
        name = 'Humidity'
        super(HumidityGraph, self).__init__(fieldNameLineTup, units, name, fig, pos, 30.0, window)

class LivePPTGraph:

    def __init__(self, ppt, window = 60):

        self.ppt = ppt
        self.window = window
        self.slice = -window
        self.interval = ppt.readEepromConfig()['globalSampleRate']
        self.pptIter = ppt.getLiveGenerator()

        self.fig = plt.figure(1, figsize = (18,10))

        self.graphs = []
        self.graphs.append(AccelGraph(self.fig, '231'))
        self.graphs.append(PressureGraph(self.fig, '232'))
        self.graphs.append(LightGraph(self.fig, '233'))
        self.graphs.append(TemperatureGraph(self.fig, '234'))
        self.graphs.append(HumidityGraph(self.fig, '235'))

        gobject.timeout_add(self.interval - 5, self.animate)
        plt.show()

    def animate(self):
        
        d = self.pptIter.next()

        for graph in self.graphs:
            graph.animate(d)
        
        self.fig.canvas.draw()

        return True

        
            
p = pptscanner.findPPTs()[0]
