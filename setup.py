from distutils.core import setup
setup(name='lcmap-cli',
      version='0.5',
      py_modules=['cli'],
      entry_points={ 'console_scripts': ['lcmap = cli.__main__:main' ] }
      )
