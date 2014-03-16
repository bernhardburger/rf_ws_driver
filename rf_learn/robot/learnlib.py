'''
Created on 15.03.2014

@author: USER
'''

class learnlib:
    '''
    classdocs
    '''
    ROBOT_LIBRARY_SCOPE = 'GLOBAL'
    __version__ = '0.0.1'
    
    def __init__(self):
        '''
        Constructor
        '''
        self._name = 'learnLib'
        
    def simple_keyword(self):
        """Log a message"""
    
        print 'You have used the simplest keyword.'

    def greet(self, name):
        """Logs a friendly greeting to person given as argument"""
        print 'Hello %s!' % name

    def multiply_by_two(self, number):
        """Returns the given number multiplied by two
        
        The result is always a floating point number.
        This keyword fails if the given `number` cannot be converted to number.
        """
        return float(number) * 2

    def numbers_should_be_equal(self, first, second):
        print '*DEBUG* Got arguments %s and %s' % (first, second)
        if float(first) != float(second):
            raise AssertionError('Given numbers are unequal!') 
    

    
        