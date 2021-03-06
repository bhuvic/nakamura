#!/usr/bin/env ruby

require 'rubygems'
require 'bundler'
Bundler.setup(:default)
require 'nakamura/test'
include SlingUsers

class TC_Kern1887Test < Test::Unit::TestCase
  include SlingTest

  def setup
    super
    m = uniqueness()
    @user = create_user("user-#{m}")
    @home = @user.home_path_for(@s)

    @s.switch_user(@user)
    @s.execute_post(@s.url_for("#{@home}.modifyAce.html"), {
      "principalId" => "everyone",
      "privilege@jcr:read" => "granted"
    })

    res = @s.execute_get(@s.url_for("#{@home}.acl.json"))
    assert_equal(["Read"], JSON.parse(res.body)["everyone"]["granted"])
  end

  def test_remove_read_does_not_grant_more_access
    # Now explicitly deny all and make sure their grants is empty
    @s.execute_post(@s.url_for("#{@home}.modifyAce.html"), {
      "principalId" => "everyone",
      "privilege@jcr:all" => "denied"
    })

    res = @s.execute_get(@s.url_for("#{@home}.acl.json"))
    assert_equal([], JSON.parse(res.body)["everyone"]["granted"])
  end


  def test_set_to_none_clears_all
    # Clear all ACLs
    @s.execute_post(@s.url_for("#{@home}.modifyAce.html"), {
      "principalId" => "everyone",
      "privilege@jcr:read" => "none"
    })

    res = @s.execute_get(@s.url_for("#{@home}.acl.json"))
    json = JSON.parse(res.body)["everyone"]
    assert_equal([], json["granted"])
    assert_equal([], json["denied"])
  end

end
