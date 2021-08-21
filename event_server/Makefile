.PHONY: all repl

all:
	@ rebar3 clojerl compile

repl:
	@ rebar3 clojerl repl --apps try_clojerl

clean:
	@ rm -rf _build rebar.lock

release:
	@ rebar3 as prod do clojerl compile, clojerl release

